package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类 - 用于延时消息队列
 * 使用RabbitMQ延迟消息插件(rabbitmq-delayed-message-exchange)
 * 支持灵活的延迟时间控制
 */
@Configuration
public class RabbitMqConfig {

    // 延时队列交换机和队列名称
    public static final String MOTOR_CONTROL_DELAY_EXCHANGE = "motor.control.delay.exchange";
    public static final String MOTOR_CONTROL_DELAY_QUEUE = "motor.control.delay.queue";
    public static final String MOTOR_CONTROL_DELAY_ROUTING_KEY = "motor.control.delay.routingkey";

    // 实际处理交换机和队列名称
    public static final String MOTOR_CONTROL_EXCHANGE = "motor.control.exchange";
    public static final String MOTOR_CONTROL_QUEUE = "motor.control.queue";
    public static final String MOTOR_CONTROL_ROUTING_KEY = "motor.control.routingkey";

//    @Value("${rabbitmq.motor.delay-time:5000}")
//    private Integer motorDelayTime;

    /**
     * 创建延时消息交换机
     * 使用x-delayed-message类型实现灵活的延迟时间控制
     */
    @Bean
    public CustomExchange motorControlDelayExchange() {
        return new CustomExchange(MOTOR_CONTROL_DELAY_EXCHANGE, "x-delayed-message", true, false,
                java.util.Collections.singletonMap("x-delayed-type", "direct"));
    }

    /**
     * 创建延时消息队列
     * 此队列接收延时消息，消息延迟后会发送到实际处理交换机
     */
    @Bean
    public Queue motorControlDelayQueue() {
        return QueueBuilder.durable(MOTOR_CONTROL_DELAY_QUEUE).build();
    }

    /**
     * 将延时队列绑定到延时交换机
     */
    @Bean
    public Binding motorControlDelayBinding(Queue motorControlDelayQueue, CustomExchange motorControlDelayExchange) {
        return new Binding(
                motorControlDelayQueue.getName(),
                Binding.DestinationType.QUEUE,
                motorControlDelayExchange.getName(),
                MOTOR_CONTROL_DELAY_ROUTING_KEY,
                null
        );
    }

    /**
     * 创建实际处理交换机
     * 延时消息到期后会路由到此交换机
     */
    @Bean
    public DirectExchange motorControlExchange() {
        return new DirectExchange(MOTOR_CONTROL_EXCHANGE, true, false);
    }

    /**
     * 创建实际处理队列
     * 最终处理电机控制消息的队列
     */
    @Bean
    public Queue motorControlQueue() {
        return QueueBuilder.durable(MOTOR_CONTROL_QUEUE).build();
    }

    /**
     * 将处理队列绑定到处理交换机
     */
    @Bean
    public Binding motorControlBinding(Queue motorControlQueue, DirectExchange motorControlExchange) {
        return BindingBuilder.bind(motorControlQueue)
                .to(motorControlExchange)
                .with(MOTOR_CONTROL_ROUTING_KEY);
    }
}
