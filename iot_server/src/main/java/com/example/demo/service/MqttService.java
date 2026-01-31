package com.example.demo.service;

import com.example.demo.entity.Device;
import com.example.demo.entity.MotorFan;
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
import com.example.demo.entity.Sensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService implements MqttCallback {


    /**
     * 设备向服务器发送 mqtt 消息时的 topic
     * 如  device/d002
     */
    public static final String DEVICE_REPORT = "device/";
    /**
     * 服务器向设备发送 mqtt时的 topic
     */
    public static String DEVICE_CTRL(String deviceNum){
        return  "device-ctrl/"+deviceNum;
    };

    /**
     * 服务器向 前端 发送更新通知的 topic
     */
    public static final String WX_CTRL = "";

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
        System.out.println("topic : " + topic);
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
                    processMotorControlRules(device.getId(), device.getDeviceNum());

                    // 数据已经更新，发消息给前端更新数据
                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("topic", topic);
                    messageMap.put("payload", "UPDATE_DEVICES");
                    // qos 1 确保消息到达
                    MqttMessage mqttMessage = new MqttMessage(objectMapper.writeValueAsBytes(messageMap));
                    mqttMessage.setQos(1);
                    client.publish("wxapi/" + deviceNum, mqttMessage);

                }
            }

        } catch (Exception e) {
            log.error("MQTT payload parse error", e);
        }
    }

    /**
     * 处理设备所有电机的控制规则
     * 从数据库读取传感器和电机配置，应用控制规则
     *
     * @param deviceId  设备ID
     * @param deviceNum
     */
    private void processMotorControlRules(Long deviceId, String deviceNum) {
        try {
            // 获取该设备的所有电机
            List<MotorFan> motors = motorFanService.findByParentId(deviceId);

            if (motors == null || motors.isEmpty()) {
                return;
            }

            // 为每个电机处理控制规则
            // for在循环前，一次性获取所有传感器
            List<Sensor> sensors = sensorService.findByDeviceId(deviceId);

            // 创建 sensorId -> sensor 的映射，便于快速查找
            Map<Long, Sensor> sensorMap = new HashMap<>();
            if (sensors != null) {
                for (Sensor sensor : sensors) {
                    sensorMap.put(sensor.getId(), sensor);
                }
            }

            // 为每个电机处理控制规则
            for (MotorFan motor : motors) {
                try {
                    // 获取该电机的探头传感器ID
                    Long probeSensorId = motor.getProbeSensorId();
                    Double currentSensorValue = null;

                    if (probeSensorId != null) {
                        // 从内存中的 sensorMap 获取传感器，避免多次数据库查询
                        Sensor sensor = sensorMap.get(probeSensorId);
                        if (sensor != null && sensor.getSensorValue() != null) {
                            currentSensorValue = sensor.getSensorValue();
                        }
                    }
                    // 应用控制规则
                    motorControlRuleEngineService.processMotorControl(motor, currentSensorValue, deviceNum);

                } catch (Exception e) {
                    log.error("处理电机控制规则错误: motorId={}", motor.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("处理电机控制规则错误: deviceId={}", deviceId, e);
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    /**
     * 发送 MQTT 消息到指定主题
     *
     * @param topic   消息主题
     * @param payload 消息内容（对象会被序列化为 JSON）
     * @param qos     服务质量等级 (0, 1, 2)，默认为 1
     * @return 发送是否成功
     */
    public boolean publishMessage(String topic, Object payload, int qos) {
        try {
            if (!client.isConnected()) {
                log.warn("MQTT client is not connected, attempting to reconnect");
                client.connect();
            }

            byte[] payloadBytes;
            if (payload instanceof String) {
                payloadBytes = ((String) payload).getBytes(StandardCharsets.UTF_8);
            } else {
                payloadBytes = objectMapper.writeValueAsBytes(payload);
            }

            MqttMessage message = new MqttMessage(payloadBytes);
            message.setQos(Math.min(qos, 2)); // QoS 范围：0-2
            message.setRetained(false);

            client.publish(topic, message);
            log.info("MQTT message published: topic={}, qos={}, payloadSize={} bytes",
                    topic, qos, payloadBytes.length);
            return true;

        } catch (MqttException e) {
            log.error("Failed to publish MQTT message to topic: {}", topic, e);
            return false;
        } catch (Exception e) {
            log.error("Error serializing MQTT payload for topic: {}", topic, e);
            return false;
        }
    }

    /**
     * 发送 MQTT 消息到指定主题 (QoS 默认为 1)
     *
     * @param topic   消息主题
     * @param payload 消息内容
     * @return 发送是否成功
     */
    public boolean publishMessage(String topic, Object payload) {
        return publishMessage(topic, payload, 1);
    }

    /**
     * 发送 MQTT 消息到指定主题 (消息为字符串，QoS 默认为 1)
     *
     * @param topic   消息主题
     * @param message 消息内容（字符串）
     * @return 发送是否成功
     */
    public boolean publishString(String topic, String message) {
        return publishMessage(topic, message, 1);
    }

    /**
     * 检查 MQTT 连接状态
     *
     * @return 如果连接则返回 true，否则返回 false
     */
    public boolean isConnected() {
        return client != null && client.isConnected();
    }
}

