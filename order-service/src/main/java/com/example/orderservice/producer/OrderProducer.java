package com.example.orderservice.producer;
import com.example.commonmodels.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderProducer {
    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);
    private final RabbitTemplate rabbitTemplate;
    private static final String ORDER_EXCHANGE = "order.exchange";
    private static final String ORDER_DIRECT_EXCHANGE = "order.direct.exchange";
    private static final String ORDER_FANOUT_EXCHANGE = "order.fanout.exchange";

    public OrderProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    public void sendOrderEvent(Order order, String eventType) {
        String routingKey = "order." + eventType;
        log.info("Sending order event {} for order: {}", eventType, order.getOrderId());
        this.rabbitTemplate.convertAndSend(ORDER_EXCHANGE, routingKey, order);
    }

    public void sendOrderDirect(Order order, String routingKey) {
        log.info("Sending order directly: {}", routingKey);
        this.rabbitTemplate.convertAndSend(ORDER_DIRECT_EXCHANGE, routingKey, order);
    }

    public void broadcastOrder(Order order) {
        log.info("Broadcasting order: {}", order.getOrderId());
        this.rabbitTemplate.convertAndSend(ORDER_FANOUT_EXCHANGE, "", order);
    }


    public void sendOrder(String exchange, String routingKey, Object message) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        // В Spring Boot 3.x используется CompletableFuture:
        correlationData.getFuture().whenComplete((confirm, throwable) -> {
            if (throwable != null) {
                log.error("Ошибка при подтверждении отправки сообщения", throwable);
            } else if (confirm != null && confirm.isAck()) {
                log.info("Сообщение успешно доставлено брокеру (ACK), ID: {}", correlationData.getId());
            } else {
                log.error("Сообщение отклонено (NACK), причина: {}",
                        confirm != null ? confirm.getReason() : "Неизвестно");
            }
        });

        this.rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
    }
    public void sendOrderWithConfirm(Order order, String eventType) {
        String routingKey = "order." + eventType;
        String correlationId = UUID.randomUUID().toString();

        CorrelationData correlationData = new CorrelationData(correlationId);
        // Spring Boot 3.x implementation
        correlationData.getFuture().whenComplete((confirm, throwable) -> {
            if (throwable != null) {
                log.error("Ошибка при подтверждении отправки сообщения", throwable);
            } else if (confirm != null && confirm.isAck()) {
                log.info("Сообщение успешно доставлено брокеру (ACK), ID: {}", correlationData.getId());
            } else {
                log.error("Сообщение отклонено (NACK), причина: {}",
                        confirm != null ? confirm.getReason() : "Неизвестно");
            }
        });

        this.rabbitTemplate.convertAndSend(ORDER_EXCHANGE, routingKey, order, correlationData);
    }

    public CompletableFuture<Order> sendOrderWithReply(Order order) {
        return sendOrderAsync(ORDER_DIRECT_EXCHANGE, "order.process", order);
    }

    public CompletableFuture<Order> sendOrderAsync(String exchange, String routingKey, Order order) {
        return CompletableFuture.supplyAsync(() ->
                (Order) this.rabbitTemplate.convertSendAndReceive(exchange, routingKey, order)
        );
    }
    public void sendHighPriorityOrder(Order order, String eventType) {
        String routingKey = "order." + eventType;
        this.rabbitTemplate.convertAndSend(ORDER_EXCHANGE, routingKey, order, message -> {
            message.getMessageProperties().setPriority(10);
            return message;
        });
    }
}
