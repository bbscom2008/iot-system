package com.example.demo.service;

import com.example.demo.config.RabbitMqConfig;
import com.example.demo.dto.MotorControlMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 电机控制消息消费者服务
 * 处理来自RabbitMQ的电机控制消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotorControlConsumerService {

    private final MotorFanService motorFanService;

    private final MqttService mqttService;

    private final ObjectMapper objectMapper;
    
    /**
     * 从电机控制队列消费电机控制消息
     * @param message 电机控制消息
     */
    @RabbitListener(queues = RabbitMqConfig.MOTOR_CONTROL_QUEUE)
    public void processMotorControlMessage(MotorControlMessage message) {
        try {
            if (message == null) {
                log.warn("接收到空的电机控制消息");
                return;
            }
            
            log.info("【消费者】处理电机控制消息: motorId={}, state={}, timestamp={}", 
                    message.getMotorId(), message.getState(), message.getTimestamp());
            
            // 验证消息内容
            if (message.getMotorId() == null ) {
                log.error("电机ID为空，无法处理消息");
                return;
            }
            
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("id", message.getParentDeviceNum());
            jsonMap.put(message.getDeviceNum(), message.getState());

            String payload = objectMapper.writeValueAsString(jsonMap);
            
            // 发送 MQTT 消息
            boolean publishSuccess = mqttService.publishString("device-ctrl/" + message.getParentDeviceNum(), payload);
            if (!publishSuccess) {
                log.warn("MQTT消息发送失败: getParentDeviceNum={}, motorId={}", message.getParentDeviceNum(), message.getMotorId());
            }
            
            log.info("【消费者】电机控制消息处理成功: motorId={}, state={}", 
                    message.getMotorId(), message.getState());
            
        } catch (Exception e) {
            log.error("【消费者】处理电机控制消息错误: {}", 
                    message != null ? message.getMotorId() : "unknown", e);
            // 注意：自动确认模式下异常不会导致消息重新入队
            // 如需重试机制，应改为手动确认模式
        }
    }
}
