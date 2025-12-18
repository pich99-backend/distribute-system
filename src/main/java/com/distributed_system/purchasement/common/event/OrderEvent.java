package com.distributed_system.purchasement.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event for order-related operations
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderEvent extends BaseEvent {

    private Long orderId;
    private Long userId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String status; // CREATED, PROCESSING, COMPLETED, CANCELLED
    private String action; // CREATED, UPDATED, CANCELLED, COMPLETED

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal price;
    }

    /**
     * Create an order created event
     */
    public static OrderEvent created(Long orderId, Long userId, List<OrderItem> items, BigDecimal totalAmount) {
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .items(items)
                .totalAmount(totalAmount)
                .status("CREATED")
                .action("CREATED")
                .build();
        event.initializeMetadata("ORDER_CREATED", "purchasement-service");
        return event;
    }

    /**
     * Create an order cancelled event
     */
    public static OrderEvent cancelled(Long orderId, Long userId) {
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .status("CANCELLED")
                .action("CANCELLED")
                .build();
        event.initializeMetadata("ORDER_CANCELLED", "purchasement-service");
        return event;
    }

    /**
     * Create an order completed event
     */
    public static OrderEvent completed(Long orderId, Long userId, BigDecimal totalAmount) {
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .totalAmount(totalAmount)
                .status("COMPLETED")
                .action("COMPLETED")
                .build();
        event.initializeMetadata("ORDER_COMPLETED", "purchasement-service");
        return event;
    }
}

