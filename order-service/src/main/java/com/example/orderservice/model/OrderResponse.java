package com.example.orderservice.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private String orderId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private Double amount;
    private String status;
    private String itemType;
    private Integer quantity;
    private LocalDateTime createdAt;
    private String message;
}
