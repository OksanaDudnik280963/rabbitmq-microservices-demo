package com.example.commonmodels;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private String notificationId;
    private String userId;
    private String userEmail;
    private String message;
    private String type;
    private String eventType;
    private LocalDateTime sentAt;
}
