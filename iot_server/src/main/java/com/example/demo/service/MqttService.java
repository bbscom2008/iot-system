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
import com.example.demo.service.FrequencyMotorService;
import com.example.demo.service.MqttMessageDataService;
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
                    // 更新设备为在线状态
                    deviceService.markDeviceOnline(deviceNum);
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
                    if (!freqMotorValues.isEmpty()) {
                        frequencyMotorService.batchUpdateValueByParentId(parentId, freqMotorValues);
                    }
                }
            }
        } catch (Exception e) {
            log.error("MQTT payload parse error", e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
