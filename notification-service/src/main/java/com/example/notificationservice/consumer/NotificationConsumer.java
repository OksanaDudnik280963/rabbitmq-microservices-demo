package com.example.notificationservice.consumer;

import com.example.commonmodels.Order;
import com.example.notificationservice.config.RabbitMQConfig;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_ORDER_QUEUE)
    public void processOrderEvent(
            Order order,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        log.info("Notification service received {} for order {}", routingKey, order.getOrderId());
        String eventType = routingKey.replace("order.", "");

        switch (eventType) {
            case "created" -> processOrderCreated(order);
            case "updated" -> processOrderUpdated(order);
            case "cancelled" -> processOrderCancelled(order);
            default -> log.warn("Unsupported order event: {}", eventType);
        }
    }

    private void processOrderCreated(Order order) {
        this.notificationService.sendEmailNotification(
                order.getCustomerId(),
                order.getCustomerEmail(),
                "Your order " + order.getOrderId() + " has been created successfully.",
                "ORDER_CREATED"
        );
    }

    private void processOrderUpdated(Order order) {
        this.notificationService.sendEmailNotification(
                order.getCustomerId(),
                order.getCustomerEmail(),
                "Your order " + order.getOrderId() + " was updated. Current status: " + order.getStatus(),
                "ORDER_UPDATED"
        );
    }

    private void processOrderCancelled(Order order) {
        this.notificationService.sendEmailNotification(
                order.getCustomerId(),
                order.getCustomerEmail(),
                "Your order " + order.getOrderId() + " has been cancelled.",
                "ORDER_CANCELLED"
        );
        this.notificationService.sendSmsNotification(
                order.getCustomerId(),
                "Order " + order.getOrderId() + " cancelled.",
                "ORDER_CANCELLED"
        );
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_EMAIL_QUEUE)
    public void processOrderCreatedEmail(Order order) {
        log.info("Dedicated email listener received order {}", order.getOrderId());
        this.notificationService.sendEmailNotification(
                order.getCustomerId(),
                order.getCustomerEmail(),
                "Thank you for your order. Order ID: " + order.getOrderId(),
                "ORDER_CREATED_EMAIL"
        );
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_ALL_QUEUE)
    public void processAllOrderEvents(Order order) {
        log.info("Audit notification event recorded for order {}", order.getOrderId());
    }
}