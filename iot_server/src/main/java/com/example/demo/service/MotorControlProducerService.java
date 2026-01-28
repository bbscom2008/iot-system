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
                            log.info("【生产者】发送延时电机控制消息: motorId={}, delay={}ms, state={}", 
                                    motorControlMessage.getMotorId(), motorControlMessage.getDelayTime(),
                                    motorControlMessage.getState());
                        } else {
                            log.info("【生产者】发送电机控制消息（延时0）: motorId={}, state={}", 
                                    motorControlMessage.getMotorId(), motorControlMessage.getState());
                        }
                        return message;
                    }
            );
        } catch (Exception e) {
            log.error("【生产者】发送延时电机控制消息错误: motorId={}", 
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
            log.info("【生产者】发送电机控制消息到处理队列: motorId={}, state={}, exchange={}, routingKey={}", 
                    motorControlMessage.getMotorId(), motorControlMessage.getState(),
                    RabbitMqConfig.MOTOR_CONTROL_EXCHANGE,
                    RabbitMqConfig.MOTOR_CONTROL_ROUTING_KEY);
        } catch (Exception e) {
            log.error("【生产者】发送电机控制消息错误: motorId={}", 
                    motorControlMessage.getMotorId(), e);
        }
    }
}
