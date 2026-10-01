package com.example.commonmodels;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String orderId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private String status;
    private String itemType;
    private Integer quantity;
    private LocalDateTime createdAt;
}