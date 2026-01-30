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

    private final MotorControlRuleEngineService motorControlService;

    private final MqttService mqttService;

    private final ObjectMapper objectMapper;

    /**
     * 从电机控制队列消费电机控制消息
     *
     * @param message 电机控制消息
     */
    @RabbitListener(queues = RabbitMqConfig.MOTOR_CONTROL_QUEUE)
    public void processMotorControlMessage(MotorControlMessage message) {
        try {
            log.info("【消费者】处理电机控制消息: motorId={}, state={}, timestamp={}",
                    message.getMotorId(), message.getState(), message.getTimestamp());

            motorControlService.updateMotorFanState(message.getDeviceNum(),
                    message.getState(), message.getParentDeviceNum());

        } catch (Exception e) {
            log.error("【消费者】处理电机控制消息错误: {}",
                    message != null ? message.getMotorId() : "unknown", e);
        }
    }
}
