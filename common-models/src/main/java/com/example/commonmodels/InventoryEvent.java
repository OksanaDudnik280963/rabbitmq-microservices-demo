package com.example.commonmodels;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryEvent {
    private String eventId;
    private String orderId;
    private String itemType;
    private Integer quantity;
    private String action;
    private String status;
    private String message;
    private LocalDateTime timestamp;
}
