package com.example.demo.service;

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
import java.util.HashMap;
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
    private final MqttMessageRecordService mqttMessageRecordService;
    private final FrequencyMotorService frequencyMotorService;

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
            mqttMessageRecordService.save(payload);
            JsonNode idNode = node.get("id");
            if (idNode != null && idNode.isTextual()) {
                String deviceNum = idNode.asText();
                deviceService.markDeviceOnline(deviceNum);
                Device device = deviceService.findByDeviceNum(deviceNum);
                if (device != null) {
                    Long parentId = device.getId();
                    
                    // 批量更新传感器值
                    Map<String, Double> sensorValues = new HashMap<>();
                    for (int i = 1; i <= 4; i++) {
                        String key = "ts" + i;
                        JsonNode v = node.get(key);
                        if (v != null && v.isNumber()) {
                            sensorValues.put(key, v.asDouble());
                        }
                    }
                    if (!sensorValues.isEmpty()) {
                        sensorService.batchUpdateValueByParentId(parentId, sensorValues);
                    }
                    
                    // 批量更新电机运行状态
                    Map<String, Integer> motorValues = new HashMap<>();
                    for (int i = 1; i <= 10; i++) {
                        String key = "mt" + i;
                        JsonNode v = node.get(key);
                        if (v != null) {
                            Integer run = null;
                            if (v.isBoolean()) {
                                run = v.booleanValue() ? 1 : 0;
                            } else if (v.isNumber()) {
                                run = v.intValue() != 0 ? 1 : 0;
                            } else if (v.isTextual()) {
                                String s = v.asText().trim();
                                if ("true".equalsIgnoreCase(s) || "1".equals(s)) run = 1;
                                else if ("false".equalsIgnoreCase(s) || "0".equals(s)) run = 0;
                            }
                            if (run != null) {
                                motorValues.put(key, run);
                            }
                        }
                    }
                    if (!motorValues.isEmpty()) {
                        motorFanService.batchUpdateRunningStatusByParentId(parentId, motorValues);
                    }
                    
                    // 批量更新变频电机的值
                    Map<String, Integer> frequencyMotorValues = new HashMap<>();
                    
                    // 处理 imt1
                    JsonNode imt1Node = node.get("imt1");
                    if (imt1Node != null && imt1Node.isNumber()) {
                        frequencyMotorValues.put("imt1", imt1Node.intValue());
                    }
                    
                    // 处理 imt2
                    JsonNode imt2Node = node.get("imt2");
                    if (imt2Node != null && imt2Node.isNumber()) {
                        frequencyMotorValues.put("imt2", imt2Node.intValue());
                    }
                    
                    // 如果有需要更新的值，调用批量更新方法
                    if (!frequencyMotorValues.isEmpty()) {
                        frequencyMotorService.batchUpdateValueByParentId(parentId, frequencyMotorValues);
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
