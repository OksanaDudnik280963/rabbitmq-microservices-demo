package com.example.orderservice.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) System.out.println("Confirmed: " + correlationData);
            else System.err.println("Failed: " + correlationData + " Cause: " + cause);
        });
        return rabbitTemplate;
    }
}
