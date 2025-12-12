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
                String deviceNum = idNode.asText();
                deviceService.markDeviceOnline(deviceNum);
                Device device = deviceService.findByDeviceNum(deviceNum);
                if (device != null) {
                    Long parentId = device.getId();
                    updateTsSensor(parentId, node, "ts1");
                    updateTsSensor(parentId, node, "ts2");
                    updateTsSensor(parentId, node, "ts3");
                    updateTsSensor(parentId, node, "ts4");
                    updateMotor(parentId, node, "mt1");
                    updateMotor(parentId, node, "mt2");
                    updateMotor(parentId, node, "mt3");
                    updateMotor(parentId, node, "mt4");
                    updateMotor(parentId, node, "mt5");
                    updateMotor(parentId, node, "mt6");
                    updateMotor(parentId, node, "mt7");
                    updateMotor(parentId, node, "mt8");
                    updateMotor(parentId, node, "mt9");
                    updateMotor(parentId, node, "mt10");
                }
            }
        } catch (Exception e) {
            log.error("MQTT payload parse error", e);
        }
    }

    private void updateTsSensor(Long parentId, JsonNode node, String key) {
        JsonNode v = node.get(key);
        if (v != null && v.isNumber()) {
            sensorService.updateValueByParentAndCode(parentId, key, v.asDouble());
        }
    }

    private void updateMotor(Long parentId, JsonNode node, String key) {
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
                motorFanService.updateRunningStatusByParentAndCode(parentId, key, run);
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
