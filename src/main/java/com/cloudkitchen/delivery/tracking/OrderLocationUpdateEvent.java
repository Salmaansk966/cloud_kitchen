package com.cloudkitchen.delivery.tracking;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * Event payload broadcast to customers for a specific order's live location.
 */
@Getter
@AllArgsConstructor
public class OrderLocationUpdateEvent {

    private final Long orderId;
    private final Long partnerId;
    private final Double latitude;
    private final Double longitude;
    private final OffsetDateTime timestamp;
}



