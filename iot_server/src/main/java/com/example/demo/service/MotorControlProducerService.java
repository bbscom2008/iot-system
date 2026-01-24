package com.example.demo.service;

import com.example.demo.config.RabbitMqConfig;
import com.example.demo.dto.MotorControlMessage;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * 电机控制消息生产者服务
 * 向RabbitMQ发送电机控制消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotorControlProducerService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送延时电机控制消息到延时队列
     * @param motorControlMessage 电机控制消息对象
     */
    public void sendMotorControlDelayMessage(MotorControlMessage motorControlMessage) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.MOTOR_CONTROL_DELAY_EXCHANGE,
                    RabbitMqConfig.MOTOR_CONTROL_DELAY_ROUTING_KEY,
                    motorControlMessage,
                    message -> {
                        // 设置延迟时间（毫秒）
                        // 使用rabbitmq-delayed-message-exchange插件支持
                        if (motorControlMessage.getDelayTime() != null && motorControlMessage.getDelayTime() > 0) {
                            message.getMessageProperties().setHeader("x-delay", motorControlMessage.getDelayTime());
                        }
                        return message;
                    }
            );
            log.info("发送延时电机控制消息: motorId={}, delay={}", 
                    motorControlMessage.getMotorId(), motorControlMessage.getDelayTime());
        } catch (Exception e) {
            log.error("发送延时电机控制消息错误: motorId={}", 
                    motorControlMessage.getMotorId(), e);
        }
    }

    /**
     * 直接发送电机控制消息到处理队列（无延时）
     * @param motorControlMessage 电机控制消息对象
     */
    public void sendMotorControlMessage(MotorControlMessage motorControlMessage) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.MOTOR_CONTROL_EXCHANGE,
                    RabbitMqConfig.MOTOR_CONTROL_ROUTING_KEY,
                    motorControlMessage
            );
            log.info("发送电机控制消息: motorId={}, state={}", 
                    motorControlMessage.getMotorId(), motorControlMessage.getState());
        } catch (Exception e) {
            log.error("发送电机控制消息错误: motorId={}", 
                    motorControlMessage.getMotorId(), e);
        }
    }
}
