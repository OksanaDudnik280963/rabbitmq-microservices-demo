package com.example.notificationservice.service;

import com.example.commonmodels.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class NotificationService {

    private final Map<String, Notification> sentNotifications =
            new ConcurrentHashMap<>();

    private final Map<String, Integer> notificationStatistics =
            new ConcurrentHashMap<>();

    public Notification sendEmailNotification(
            String userId,
            String userEmail,
            String message,
            String eventType) {

        Notification notification = createNotification(
                userId,
                userEmail,
                message,
                "EMAIL",
                eventType
        );

        log.info(
                "Email notification sent to {}: {}",
                userEmail,
                message
        );

        return notification;
    }

    public Notification sendSmsNotification(
            String userId,
            String message,
            String eventType) {

        Notification notification = createNotification(
                userId,
                null,
                message,
                "SMS",
                eventType
        );

        log.info(
                "SMS notification sent to {}: {}",
                userId,
                message
        );

        return notification;
    }

    public Notification sendPushNotification(
            String userId,
            String message,
            String eventType) {

        Notification notification = createNotification(
                userId,
                null,
                message,
                "PUSH",
                eventType
        );

        log.info(
                "Push notification sent to {}: {}",
                userId,
                message
        );

        return notification;
    }

    private Notification createNotification(
            String userId,
            String userEmail,
            String message,
            String type,
            String eventType) {

        Notification notification = new Notification();

        notification.setNotificationId(
                "NOTIF-" + UUID.randomUUID()
        );

        notification.setUserId(userId);
        notification.setUserEmail(userEmail);
        notification.setMessage(message);
        notification.setType(type);
        notification.setEventType(eventType);
        notification.setSentAt(LocalDateTime.now());

        this.sentNotifications.put(
                notification.getNotificationId(),
                notification
        );

        this.notificationStatistics.merge(
                type,
                1,
                Integer::sum
        );

        return notification;
    }

    public Map<String, Notification> getSentNotifications() {
        return new HashMap<>(this.sentNotifications);
    }

    public Map<String, Integer> getNotificationStatistics() {
        return new HashMap<>(this.notificationStatistics);
    }
}