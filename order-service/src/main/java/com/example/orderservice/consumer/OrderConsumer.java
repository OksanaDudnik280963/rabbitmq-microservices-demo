package com.example.orderservice.consumer;

import com.example.commonmodels.Order;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = "order.created.queue")
    public void processOrderCreated(Order order) {
        log.info("=== ORDER CREATED === Order ID: {}", order.getOrderId());
        this.orderService.handleOrderCreated(order);
    }

    @RabbitListener(queues = "order.updated.queue")
    public void processOrderUpdated(Order order) {
        log.info("=== ORDER UPDATED === Order ID: {}", order.getOrderId());
        this.orderService.handleOrderUpdated(order);
    }

    @RabbitListener(queues = "order.cancelled.queue")
    public void processOrderCancelled(Order order) {
        log.info("=== ORDER CANCELLED === Order ID: {}", order.getOrderId());
        this.orderService.handleOrderCancelled(order);
    }

    @RabbitListener(queues = "order.all.queue")
    public void processAllOrderEvents(Order order, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        log.info("=== ALL EVENTS === Routing: {}, Order: {}", routingKey, order.getOrderId());
        this.orderService.logOrderEvent(order, routingKey);
    }

    @RabbitListener(queues = "order.dlq.queue")
    public void processDeadLetter(Order order) {
        log.error("=== DEAD LETTER === Order: {}", order.getOrderId());
        this.orderService.handleFailedOrder(order);
    }
}