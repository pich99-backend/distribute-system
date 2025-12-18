package com.distributed_system.purchasement.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Event for payment-related operations
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentEvent extends BaseEvent {

    private Long paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, PAYPAL, etc.
    private String status; // INITIATED, PROCESSING, COMPLETED, FAILED
    private String failureReason;
    private String transactionId;

    /**
     * Create a payment initiated event
     */
    public static PaymentEvent initiated(Long paymentId, Long orderId, Long userId,
                                         BigDecimal amount, String currency, String paymentMethod) {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .currency(currency)
                .paymentMethod(paymentMethod)
                .status("INITIATED")
                .build();
        event.initializeMetadata("PAYMENT_INITIATED", "purchasement-service");
        return event;
    }

    /**
     * Create a payment completed event
     */
    public static PaymentEvent completed(Long paymentId, Long orderId, String transactionId) {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .status("COMPLETED")
                .transactionId(transactionId)
                .build();
        event.initializeMetadata("PAYMENT_COMPLETED", "purchasement-service");
        return event;
    }

    /**
     * Create a payment failed event
     */
    public static PaymentEvent failed(Long paymentId, Long orderId, String failureReason) {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .status("FAILED")
                .failureReason(failureReason)
                .build();
        event.initializeMetadata("PAYMENT_FAILED", "purchasement-service");
        return event;
    }
}

