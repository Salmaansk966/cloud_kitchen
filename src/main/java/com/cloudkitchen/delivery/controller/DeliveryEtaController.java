package com.cloudkitchen.delivery.controller;

import com.cloudkitchen.delivery.optimization.eta.DeliveryEtaService;
import com.cloudkitchen.delivery.optimization.eta.DeliveryEtaService.EtaResult;
import com.cloudkitchen.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for delivery ETA prediction.
 */
@RestController
@RequestMapping("/api/eta")
@RequiredArgsConstructor
public class DeliveryEtaController {

    private final DeliveryEtaService deliveryEtaService;

    /**
     * Get ETA for a given order.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<EtaResult>> getEtaForOrder(@PathVariable Long orderId) {
        EtaResult result = deliveryEtaService.predictEtaForOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("ETA calculated", result));
    }
}



