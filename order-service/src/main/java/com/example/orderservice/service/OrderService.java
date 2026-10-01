package com.example.orderservice.service;
import com.example.commonmodels.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OrderService {
    private final Map<String, Order> orderStore = new ConcurrentHashMap<>();
    private final Map<String, Integer> eventLog = new ConcurrentHashMap<>();

    public Order createOrder(String customerId, String customerName, String customerEmail,
                             Double amount, String itemType, Integer quantity) {
        Order order = new Order();
        order.setOrderId("ORD-" + System.currentTimeMillis());
        order.setCustomerId(customerId);
        order.setCustomerName(customerName);
        order.setCustomerEmail(customerEmail);
        order.setAmount(java.math.BigDecimal.valueOf(amount));
        order.setStatus("PENDING");
        order.setItemType(itemType);
        order.setQuantity(quantity);
        order.setCreatedAt(LocalDateTime.now());
        this.orderStore.put(order.getOrderId(), order);
        log.info("Order created: {}", order.getOrderId());
        return order;
    }

    public void handleOrderCreated(Order order) {
        log.info("Handling ORDER CREATED: {}", order.getOrderId());
        Order existing = this.orderStore.get(order.getOrderId());
        if (existing != null) existing.setStatus("CONFIRMED");
    }

    public void handleOrderUpdated(Order order) {
        log.info("Handling ORDER UPDATED: {}", order.getOrderId());
        Order existing = this.orderStore.get(order.getOrderId());
        if (existing != null) {
            existing.setStatus(order.getStatus());
            existing.setAmount(order.getAmount());
        }
    }

    public void handleOrderCancelled(Order order) {
        log.info("Handling ORDER CANCELLED: {}", order.getOrderId());
        Order existing = this.orderStore.get(order.getOrderId());
        if (existing != null) existing.setStatus("CANCELLED");
    }

    public void logOrderEvent(Order order, String routingKey) {
        String eventType = routingKey.replace("order.", "");
        this.eventLog.merge(eventType, 1, Integer::sum);
        log.info("Event logged: {}, Total: {}", eventType, this.eventLog.get(eventType));
    }

    public void handleFailedOrder(Order order) {
        log.error("Handling FAILED order: {}", order.getOrderId());
        if (this.orderStore.containsKey(order.getOrderId())) {
            this.orderStore.get(order.getOrderId()).setStatus("FAILED");
        }
    }

    public Order getOrder(String orderId) { return this.orderStore.get(orderId); }
    public Map<String, Order> getAllOrders() { return new HashMap<>(this.orderStore); }
    public Map<String, Integer> getEventStats() { return new HashMap<>(this.eventLog); }
}

