package com.example.inventoryservice.config;

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

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String INVENTORY_EXCHANGE = "inventory.exchange";

    public static final String INVENTORY_ORDER_QUEUE =
            "inventory.order.queue";

    public static final String INVENTORY_CREATED_QUEUE =
            "inventory.created.queue";

    public static final String INVENTORY_CANCELLED_QUEUE =
            "inventory.cancelled.queue";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(INVENTORY_EXCHANGE);
    }

    @Bean
    public Queue inventoryOrderQueue() {
        return QueueBuilder
                .durable(INVENTORY_ORDER_QUEUE)
                .build();
    }

    @Bean
    public Queue inventoryCreatedQueue() {
        return QueueBuilder
                .durable(INVENTORY_CREATED_QUEUE)
                .build();
    }

    @Bean
    public Queue inventoryCancelledQueue() {
        return QueueBuilder
                .durable(INVENTORY_CANCELLED_QUEUE)
                .build();
    }

    /*
     * Receives all order events:
     * order.created
     * order.updated
     * order.cancelled
     */
    @Bean
    public Binding inventoryOrderBinding(
            Queue inventoryOrderQueue,
            TopicExchange orderExchange) {

        return BindingBuilder
                .bind(inventoryOrderQueue)
                .to(orderExchange)
                .with("order.*");
    }

    /*
     * Receives only order.created events.
     */
    @Bean
    public Binding inventoryCreatedBinding(
            Queue inventoryCreatedQueue,
            TopicExchange orderExchange) {

        return BindingBuilder
                .bind(inventoryCreatedQueue)
                .to(orderExchange)
                .with("order.created");
    }

    /*
     * Receives only order.cancelled events.
     */
    @Bean
    public Binding inventoryCancelledBinding(
            Queue inventoryCancelledQueue,
            TopicExchange orderExchange) {

        return BindingBuilder
                .bind(inventoryCancelledQueue)
                .to(orderExchange)
                .with("order.cancelled");
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