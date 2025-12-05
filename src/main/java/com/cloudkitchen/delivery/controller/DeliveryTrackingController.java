package com.cloudkitchen.delivery.controller;

import com.cloudkitchen.delivery.tracking.DeliveryTrackingService;
import com.cloudkitchen.delivery.tracking.PartnerLocationUpdateRequest;
import com.cloudkitchen.dto.tracking.OrderRouteInfoDto;
import com.cloudkitchen.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * REST API for live partner location updates used by the delivery app.
 */
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class DeliveryTrackingController {

    private final DeliveryTrackingService trackingService;

    /**
     * Receive a partner's current location and broadcast it to customers
     * subscribed to active orders for that partner.
     */
    @PostMapping("/partner-location")
    public ResponseEntity<ApiResponse<Void>> updatePartnerLocation(
            @RequestBody PartnerLocationUpdateRequest request) throws IOException {
        trackingService.handlePartnerLocationUpdate(request);
        return ResponseEntity.ok(ApiResponse.success("Location updated", null));
    }

    /**
     * Provide latest partner/customer coordinates for a specific order.
     */
    @GetMapping("/order/{orderId}/route-info")
    public ResponseEntity<ApiResponse<OrderRouteInfoDto>> getOrderRouteInfo(
            @PathVariable Long orderId) {
        OrderRouteInfoDto data = trackingService.getOrderRouteInfo(orderId);
        return ResponseEntity.ok(ApiResponse.success("Route info fetched", data));
    }
}



