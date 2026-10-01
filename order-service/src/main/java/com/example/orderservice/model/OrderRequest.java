package com.example.orderservice.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    @NotBlank private String customerId;
    @NotBlank private String customerName;
    @NotBlank private String customerEmail;
    @NotNull @Positive private BigDecimal amount;
    @NotBlank private String itemType;
    @NotNull @Positive private Integer quantity;
}
