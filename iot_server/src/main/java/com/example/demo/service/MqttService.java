package com.example.demo.service;

import com.example.demo.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.entity.Device;
import com.example.demo.entity.MotorFan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService implements MqttCallback {

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.topic}")
    private String topic;

    @Value("${mqtt.clean-session:true}")
    private boolean cleanSession;

    @Value("${mqtt.connection-timeout:30}")
    private int connectionTimeout;

    @Value("${mqtt.keepalive-interval:60}")
    private int keepAliveInterval;

    private MqttClient client;
    private final ObjectMapper objectMapper;
    private final DeviceService deviceService;
    private final SensorService sensorService;
    private final MotorFanService motorFanService;
    private final FrequencyMotorService frequencyMotorService;
    private final MqttMessageDataService mqttMessageDataService;
    private final MotorControlRuleEngineService motorControlRuleEngineService;

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            client.setCallback(this);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(cleanSession);
            options.setConnectionTimeout(connectionTimeout);
            options.setKeepAliveInterval(keepAliveInterval);
            if (StringUtils.hasText(username)) {
                options.setUserName(username);
            }
            if (StringUtils.hasText(password)) {
                options.setPassword(password.toCharArray());
            }
            client.connect(options);
            client.subscribe(topic, 1);
            log.info("MQTT subscribed: {}", topic);
        } catch (MqttException e) {
            log.error("MQTT error", e);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            log.error("MQTT disconnect error", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        System.out.println("=====mqttmessage=====");
        System.out.println("topic : "+topic);
        System.out.println(payload);

        try {
            JsonNode node = objectMapper.readTree(payload);
            JsonNode idNode = node.get("id");
            if (idNode != null && idNode.isTextual()) {
                // 设备 ID ， 传感器设备的 父ID
                String deviceNum = idNode.asText();
                // 存储 mqtt 消息
                mqttMessageDataService.save(deviceNum, node);

                Device device = deviceService.findByDeviceNum(deviceNum);
                if (device != null) {
                    // 更新设备在线状态和报警状态
                    deviceService.updateDeviceState(device, node);

                    Long parentId = device.getId();

                    //  批量更新传感器值，如果没有对应的传感器，就创建一个新的传感器
                    List<JsonUtils.KV<Double>> sensorValues = JsonUtils.convertJsonSensors(node);
                    sensorService.batchUpdateValueByParentId(parentId, sensorValues);

                    // 批量更新电机运行状态
                    List<JsonUtils.KV<Integer>> motorValues = JsonUtils.convertJsonMotors(node);
                    motorFanService.batchUpdateRunningStatusByParentId(parentId, motorValues);

                    // 批量更新变频电机的值
                    // 处理 变频电机
                    List<JsonUtils.KV<Integer>> freqMotorValues = JsonUtils.convertJsonIMotor(node);
                    // 如果有需要更新的值，调用批量更新方法
                    frequencyMotorService.batchUpdateValueByParentId(parentId, freqMotorValues);

                    // 应用电机控制规则 - 基于自动模式和控制模式管理电机状态
                    processMotorControlRules(device.getId(), node, sensorValues);

                    // 数据已经更新，发消息给前端更新数据
                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("topic", topic);
                    messageMap.put("payload", "UPDATE_DEVICES");
                    // qos 1 确保消息到达
                    MqttMessage mqttMessage = new MqttMessage(objectMapper.writeValueAsBytes(messageMap));
                    mqttMessage.setQos(1);  
                    client.publish("wxapi/"+deviceNum, mqttMessage);

                }
            }

        } catch (Exception e) {
            log.error("MQTT payload parse error", e);
        }
    }

    /**
     * 处理设备所有电机的控制规则
     * 从motor_fan表读取电机配置并应用控制规则
     * @param deviceId 设备ID
     * @param mqttNode 包含传感器数据的MQTT消息节点
     * @param sensorValues 来自MQTT消息的传感器值
     */
    private void processMotorControlRules(Long deviceId, JsonNode mqttNode, 
                                         List<JsonUtils.KV<Double>> sensorValues) {
        try {
            // 获取该设备的所有电机
            List<MotorFan> motors = motorFanService.findByParentId(deviceId);
            
            if (motors == null || motors.isEmpty()) {
                return;
            }

            // 获取温度传感器数据 (ts1-ts4)
            Double ts1 = extractSensorValue(sensorValues, "ts1");
            Double ts2 = extractSensorValue(sensorValues, "ts2");
            Double ts3 = extractSensorValue(sensorValues, "ts3");
            Double ts4 = extractSensorValue(sensorValues, "ts4");

            // 为每个电机处理控制规则
            for (MotorFan motor : motors) {
                try {
                    // 获取该电机的探头传感器
                    Long probeSensorId = motor.getProbeSensorId();
                    Double currentSensorValue = null;

                    if (probeSensorId != null) {
                        // 根据探头传感器ID获取传感器值
                        currentSensorValue = getSensorValueById(probeSensorId, ts1, ts2, ts3, ts4);
                    } else {
                        // 默认使用第一个温度传感器
                        currentSensorValue = ts1;
                    }

                    // 应用控制规则
                    motorControlRuleEngineService.processMotorControl(motor.getId(), currentSensorValue);

                } catch (Exception e) {
                    log.error("处理电机控制规则错误: motorId={}", motor.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("处理电机控制规则错误: deviceId={}", deviceId, e);
        }
    }

    /**
     * 从传感器值列表中提取传感器值
     * @param sensorValues 传感器值列表
     * @param key 传感器键（ts1, ts2等）
     * @return 传感器值或null
     */
    private Double extractSensorValue(List<JsonUtils.KV<Double>> sensorValues, String key) {
        if (sensorValues == null) {
            return null;
        }
        return sensorValues.stream()
                .filter(kv -> key.equals(kv.getKey()))
                .map(JsonUtils.KV::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据探头传感器ID获取传感器值
     * 对于此实现，我们假设：
     * - 传感器ID 1, 2, 3, 4 映射到温度传感器 ts1, ts2, ts3, ts4
     * 如果需要，可以扩展为从数据库查询
     * @param probeSensorId 探头传感器ID
     * @param ts1 温度传感器1的值
     * @param ts2 温度传感器2的值
     * @param ts3 温度传感器3的值
     * @param ts4 温度传感器4的值
     * @return 传感器值或null
     */
    private Double getSensorValueById(Long probeSensorId, Double ts1, Double ts2, 
                                     Double ts3, Double ts4) {
        if (probeSensorId == null) {
            return ts1;
        }

        // 将传感器ID映射到实际传感器值
        // 这假设probe_sensor_id与数据库中的传感器ID相对应
        // 根据实际数据库模式调整映射
        switch (probeSensorId.intValue()) {
            case 1:
                return ts1;
            case 2:
                return ts2;
            case 3:
                return ts3;
            case 4:
                return ts4;
            default:
                return ts1; // 默认使用第一个传感器
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}

