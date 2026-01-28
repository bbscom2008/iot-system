package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类 - 用于延时消息队列
 * 使用RabbitMQ延迟消息插件(rabbitmq-delayed-message-exchange)
 * 支持灵活的延迟时间控制
 */
@Configuration
public class RabbitMqConfig {

    @Autowired
    private ConnectionFactory connectionFactory;

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

    /**
     * 配置消息转换器 - 使用Jackson2JsonMessageConverter处理JSON序列化
     * 解决Spring AMQP安全反序列化问题
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        
        // 创建类映射器，允许指定的类进行反序列化
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("*"); // 允许所有包的反序列化
        converter.setClassMapper(classMapper);
        
        return converter;
    }

    /**
     * 配置 RabbitListener 容器工厂
     * 将 Jackson2JsonMessageConverter 绑定到监听器，确保消息被正确反序列化
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);
        // 设置并发消费者数量
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(3);
        // 启用自动确认，确保消息被消费后不会重新入队
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.AUTO);
        return factory;
    }
}
