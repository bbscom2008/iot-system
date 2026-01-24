package com.example.demo.service;

import com.example.demo.config.RabbitMqConfig;
import com.example.demo.dto.MotorControlMessage;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 电机控制消息消费者服务
 * 处理来自RabbitMQ的电机控制消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotorControlConsumerService {

    private final MotorFanService motorFanService;

    /**
     * 从电机控制队列消费电机控制消息
     * @param message 电机控制消息
     */
    @RabbitListener(queues = RabbitMqConfig.MOTOR_CONTROL_QUEUE)
    public void processMotorControlMessage(MotorControlMessage message) {
        try {
            log.info("处理电机控制消息: motorId={}, state={}", 
                    message.getMotorId(), message.getState());
            
            // 更新电机运行状态
//            motorFanService.updateMotorState(message.getMotorId(), message.getState());
            
            log.info("电机控制消息处理成功: motorId={}", message.getMotorId());
        } catch (Exception e) {
            log.error("处理电机控制消息错误: motorId={}", 
                    message.getMotorId(), e);
            // 备注：如果需要可以在此实现重试逻辑
            throw new RuntimeException("Failed to process motor control message", e);
        }
    }
}
