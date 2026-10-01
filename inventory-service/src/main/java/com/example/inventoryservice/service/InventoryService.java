package com.example.inventoryservice.service;

import com.example.commonmodels.InventoryEvent;
import com.example.commonmodels.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class InventoryService {

    private final Map<String, Integer> inventory =
            new ConcurrentHashMap<>();

    private final Map<String, InventoryEvent> eventLog =
            new ConcurrentHashMap<>();

    public InventoryService() {
        this.inventory.put("LAPTOP", 50);
        this.inventory.put("PHONE", 100);
        this.inventory.put("TABLET", 75);
        this.inventory.put("HEADPHONES", 200);

        log.info("Initial inventory: {}", this.inventory);
    }

    public InventoryEvent reserveInventory(Order order) {
        String itemType = order.getItemType().toUpperCase();
        Integer requestedQuantity = order.getQuantity();

        Integer availableQuantity =
                this.inventory.getOrDefault(itemType, 0);

        if (availableQuantity >= requestedQuantity) {
            this.inventory.put(
                    itemType,
                    availableQuantity - requestedQuantity
            );

            log.info(
                    "Reserved {} {} item(s). Remaining stock: {}",
                    requestedQuantity,
                    itemType,
                    this.inventory.get(itemType)
            );

            return createEvent(
                    order.getOrderId(),
                    itemType,
                    requestedQuantity,
                    "RESERVE",
                    "SUCCESS",
                    "Inventory reserved successfully"
            );
        }

        log.warn(
                "Insufficient stock for {}. Requested: {}, Available: {}",
                itemType,
                requestedQuantity,
                availableQuantity
        );

        return createEvent(
                order.getOrderId(),
                itemType,
                requestedQuantity,
                "RESERVE",
                "INSUFFICIENT_STOCK",
                "Insufficient inventory"
        );
    }

    public InventoryEvent releaseInventory(Order order) {
        String itemType = order.getItemType().toUpperCase();
        Integer releasedQuantity = order.getQuantity();

        Integer currentQuantity =
                this.inventory.getOrDefault(itemType, 0);

        this.inventory.put(
                itemType,
                currentQuantity + releasedQuantity
        );

        log.info(
                "Released {} {} item(s). New stock: {}",
                releasedQuantity,
                itemType,
                this.inventory.get(itemType)
        );

        return createEvent(
                order.getOrderId(),
                itemType,
                releasedQuantity,
                "RELEASE",
                "SUCCESS",
                "Inventory released successfully"
        );
    }

    public boolean checkAvailability(
            String itemType,
            Integer quantity) {

        String normalizedItemType =
                itemType.toUpperCase();

        Integer available =
                this.inventory.getOrDefault(normalizedItemType, 0);

        return available >= quantity;
    }

    public Map<String, Integer> getInventoryLevels() {
        return new HashMap<>(this.inventory);
    }

    public Map<String, InventoryEvent> getEventLog() {
        return new HashMap<>(this.eventLog);
    }

    private InventoryEvent createEvent(
            String orderId,
            String itemType,
            Integer quantity,
            String action,
            String status,
            String message) {

        InventoryEvent event = new InventoryEvent(
                "INV-" + UUID.randomUUID(),
                orderId,
                itemType,
                quantity,
                action,
                status,
                message,
                LocalDateTime.now()
        );

        this.eventLog.put(event.getEventId(), event);

        return event;
    }
}