package com.example.notificationservice.controller;

import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications() {
        Map<String, Object> response = new HashMap<>();

        response.put(
                "notifications",
                this.notificationService.getSentNotifications()
        );

        response.put(
                "total",
                this.notificationService
                        .getSentNotifications()
                        .size()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> response = new HashMap<>();

        response.put(
                "statistics",
                this.notificationService
                        .getNotificationStatistics()
        );

        response.put(
                "total",
                this.notificationService
                        .getSentNotifications()
                        .size()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/test/email")
    public ResponseEntity<Map<String, String>> sendTestEmail(
            @RequestParam String email,
            @RequestParam String message) {

        this.notificationService.sendEmailNotification(
                "TEST_USER",
                email,
                message,
                "TEST_EMAIL"
        );

        return ResponseEntity.ok(
                Map.of(
                        "status", "SENT",
                        "email", email,
                        "message", message
                )
        );
    }

    @PostMapping("/test/sms")
    public ResponseEntity<Map<String, String>> sendTestSms(
            @RequestParam String userId,
            @RequestParam String message) {

        this.notificationService.sendSmsNotification(
                userId,
                message,
                "TEST_SMS"
        );

        return ResponseEntity.ok(
                Map.of(
                        "status", "SENT",
                        "userId", userId,
                        "message", message
                )
        );
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "status", "UP",
                        "service", "notification-service"
                )
        );
    }
}
