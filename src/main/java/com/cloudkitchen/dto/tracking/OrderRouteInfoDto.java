package com.cloudkitchen.dto.tracking;

import lombok.Builder;
import lombok.Value;

/**
 * DTO containing the latest partner position and the customer's delivery
 * coordinates so the frontend can plot the optimal route.
 */
@Value
@Builder
public class OrderRouteInfoDto {

    Long orderId;

    Long partnerId;

    Double partnerLat;

    Double partnerLng;

    Double deliveryLat;

    Double deliveryLng;
}


