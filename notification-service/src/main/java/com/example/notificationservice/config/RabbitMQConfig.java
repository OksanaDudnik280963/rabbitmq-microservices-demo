package com.example.notificationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE =
            "order.exchange";

    public static final String NOTIFICATION_EXCHANGE =
            "notification.exchange";

    public static final String NOTIFICATION_ORDER_QUEUE =
            "notification.order.queue";

    public static final String NOTIFICATION_EMAIL_QUEUE =
            "notification.email.queue";

    public static final String NOTIFICATION_ALL_QUEUE =
            "notification.all.queue";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue notificationOrderQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_ORDER_QUEUE)
                .build();
    }

    @Bean
    public Queue notificationEmailQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_EMAIL_QUEUE)
                .build();
    }

    @Bean
    public Queue notificationAllQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_ALL_QUEUE)
                .build();
    }

    /*
     * Receives order.created, order.updated and order.cancelled.
     */
    @Bean
    public Binding notificationOrderBinding(
            Queue notificationOrderQueue,
            TopicExchange orderExchange) {

        return BindingBuilder
                .bind(notificationOrderQueue)
                .to(orderExchange)
                .with("order.*");
    }

    /*
     * Receives only order.created events.
     */
    @Bean
    public Binding notificationEmailBinding(
            Queue notificationEmailQueue,
            TopicExchange orderExchange) {

        return BindingBuilder
                .bind(notificationEmailQueue)
                .to(orderExchange)
                .with("order.created");
    }

    /*
     * Receives all order events.
     */
    @Bean
    public Binding notificationAllBinding(
            Queue notificationAllQueue,
            TopicExchange orderExchange) {

        return BindingBuilder
                .bind(notificationAllQueue)
                .to(orderExchange)
                .with("order.#");
    }
    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. Register Java 8 date/time support (LocalDateTime, LocalDate, etc.)
        objectMapper.registerModule(new JavaTimeModule());
        // 2. Serialize dates as ISO-8601 strings rather than numeric timestamps
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);

        // 3. Configure trusted packages for deserialization
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.example.commonmodels", "com.example.*");
        converter.setJavaTypeMapper(typeMapper);

        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
