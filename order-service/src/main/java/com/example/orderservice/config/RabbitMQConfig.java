package com.example.orderservice.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order.exchange");
    }

    @Bean
    public DirectExchange orderDirectExchange() {
        return new DirectExchange("order.direct.exchange");
    }

    @Bean
    public FanoutExchange orderFanoutExchange() {
        return new FanoutExchange("order.fanout.exchange");
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable("order.created.queue").build();
    }

    @Bean
    public Queue orderUpdatedQueue() {
        return QueueBuilder.durable("order.updated.queue").build();
    }

    @Bean
    public Queue orderCancelledQueue() {
        return QueueBuilder.durable("order.cancelled.queue").build();
    }

    @Bean
    public Queue orderAllQueue() {
        return QueueBuilder.durable("order.all.queue").build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("order.dlx.exchange");
    }

    @Bean
    public Queue orderDeadLetterQueue() {
        return QueueBuilder.durable("order.dlq.queue").build();
    }

    @Bean
    public Binding orderDLQBinding(Queue orderDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(orderDeadLetterQueue).to(deadLetterExchange).with("order.dlx.routingkey");
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(orderExchange).with("order.created");
    }

    @Bean
    public Binding orderUpdatedBinding(Queue orderUpdatedQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderUpdatedQueue).to(orderExchange).with("order.updated");
    }

    @Bean
    public Binding orderCancelledBinding(Queue orderCancelledQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(orderExchange).with("order.cancelled");
    }

    @Bean
    public Binding orderAllBinding(Queue orderAllQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderAllQueue).to(orderExchange).with("order.#");
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
    }}
