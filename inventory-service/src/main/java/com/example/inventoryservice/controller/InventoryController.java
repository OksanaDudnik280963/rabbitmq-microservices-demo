package com.example.inventoryservice.controller;

import com.example.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getInventory() {
        Map<String, Object> response = new HashMap<>();

        response.put(
                "inventory",
                this.inventoryService.getInventoryLevels()
        );

        response.put(
                "timestamp",
                System.currentTimeMillis()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @RequestParam String itemType,
            @RequestParam Integer quantity) {

        boolean available =
                this.inventoryService.checkAvailability(
                        itemType,
                        quantity
                );

        Map<String, Object> response = new HashMap<>();

        response.put("itemType", itemType.toUpperCase());
        response.put("requestedQuantity", quantity);
        response.put("available", available);
        response.put(
                "message",
                available
                        ? "Inventory is available"
                        : "Insufficient inventory"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/events")
    public ResponseEntity<Map<String, Object>> getEvents() {
        Map<String, Object> response = new HashMap<>();

        response.put(
                "events",
                this.inventoryService.getEventLog()
        );

        response.put(
                "totalEvents",
                this.inventoryService.getEventLog().size()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "status", "UP",
                        "service", "inventory-service"
                )
        );
    }
}
