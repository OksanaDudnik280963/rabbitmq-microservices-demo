package com.example.orderservice.controller;
import com.example.commonmodels.Order;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.model.OrderResponse;
import com.example.orderservice.producer.OrderProducer;
import com.example.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final OrderProducer orderProducer;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        Order order = this.orderService.createOrder(request.getCustomerId(), request.getCustomerName(),
                request.getCustomerEmail(), request.getAmount().doubleValue(),
                request.getItemType(), request.getQuantity());
        this.orderProducer.sendOrderEvent(order, "created");
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.builder()
                .orderId(order.getOrderId()).customerId(order.getCustomerId())
                .customerName(order.getCustomerName()).customerEmail(order.getCustomerEmail())
                .amount(order.getAmount().doubleValue()).status(order.getStatus())
                .itemType(order.getItemType()).quantity(order.getQuantity())
                .createdAt(order.getCreatedAt()).message("Order created").build());
    }

    @PostMapping("/confirmed")
    public ResponseEntity<OrderResponse> createOrderWithConfirm(@Valid @RequestBody OrderRequest request) {
        Order order = this.orderService.createOrder(request.getCustomerId(), request.getCustomerName(),
                request.getCustomerEmail(), request.getAmount().doubleValue(),
                request.getItemType(), request.getQuantity());
        this.orderProducer.sendOrderWithConfirm(order, "created");
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.builder()
                .orderId(order.getOrderId()).message("Order created with confirmation").build());
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable String orderId, @Valid @RequestBody OrderRequest request) {
        Order existing = this.orderService.getOrder(orderId);
        if (existing == null) return ResponseEntity.badRequest().body(OrderResponse.builder().message("Not found").build());
        existing.setAmount(java.math.BigDecimal.valueOf(request.getAmount().doubleValue()));
        existing.setStatus("PROCESSING");
        this.orderProducer.sendOrderEvent(existing, "updated");
        return ResponseEntity.ok(OrderResponse.builder().orderId(existing.getOrderId())
                .message("Order updated").build());
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable String orderId) {
        Order existing = this.orderService.getOrder(orderId);
        if (existing == null) return ResponseEntity.badRequest().body(Map.of("message", "Not found"));
        existing.setStatus("CANCELLED");
        this.orderProducer.sendOrderEvent(existing, "cancelled");
        return ResponseEntity.ok(Map.of("message", "Order cancelled", "orderId", orderId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        Order order = this.orderService.getOrder(orderId);
        if (order == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(OrderResponse.builder().orderId(order.getOrderId())
                .customerId(order.getCustomerId()).customerName(order.getCustomerName())
                .amount(order.getAmount().doubleValue()).status(order.getStatus()).build());
    }

    @GetMapping
    public ResponseEntity<Map<String, OrderResponse>> getAllOrders() {
        Map<String, Order> orders = this.orderService.getAllOrders();
        Map<String, OrderResponse> response = new HashMap<>();
        orders.forEach((id, o) -> response.put(id, OrderResponse.builder()
                .orderId(o.getOrderId()).status(o.getStatus()).amount(o.getAmount().doubleValue()).build()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEventStats() {
        return ResponseEntity.ok(Map.of("eventCounts", this.orderService.getEventStats(),
                "totalOrders", this.orderService.getAllOrders().size()));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "order-service"));
    }
}
