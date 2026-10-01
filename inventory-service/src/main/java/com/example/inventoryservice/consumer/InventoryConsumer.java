package com.example.inventoryservice.consumer;

import com.example.commonmodels.InventoryEvent;
import com.example.commonmodels.Order;
import com.example.inventoryservice.config.RabbitMQConfig;
import com.example.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryConsumer {

    private final InventoryService inventoryService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_ORDER_QUEUE)
    public void processOrderEvent(
            Order order,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        log.info("Inventory service received event: {} for order: {}", routingKey, order.getOrderId());
        String eventType = routingKey.replace("order.", "");

        switch (eventType) {
            case "created" -> handleOrderCreated(order);
            case "cancelled" -> handleOrderCancelled(order);
            case "updated" -> handleOrderUpdated(order);
            default -> log.warn("Unknown order event: {}", eventType);
        }
    }

    private void handleOrderCreated(Order order) {
        log.info("Reserving inventory for order: {}", order.getOrderId());
        InventoryEvent event = this.inventoryService.reserveInventory(order);
        publishInventoryEvent(event);
    }

    private void handleOrderCancelled(Order order) {
        log.info("Releasing inventory for order: {}", order.getOrderId());
        InventoryEvent event = this.inventoryService.releaseInventory(order);
        publishInventoryEvent(event);
    }

    private void handleOrderUpdated(Order order) {
        log.info("Processing inventory update for order: {}", order.getOrderId());
    }

    private void publishInventoryEvent(InventoryEvent event) {
        String routingKey = "inventory." + event.getAction().toLowerCase();
        this.rabbitTemplate.convertAndSend(RabbitMQConfig.INVENTORY_EXCHANGE, routingKey, event);
        log.info("Published inventory event: {}", routingKey);
    }
}