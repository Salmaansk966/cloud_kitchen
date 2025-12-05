package com.cloudkitchen.delivery.controller;

import com.cloudkitchen.delivery.service.DeliveryAssignmentService;
import com.cloudkitchen.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * REST API for triggering delivery partner assignment optimization.
 */
@RestController
@RequestMapping("/api/optimization")
@RequiredArgsConstructor
public class DeliveryAssignmentController {

    private final DeliveryAssignmentService deliveryAssignmentService;

    /**
     * Trigger optimization for all READY orders without a partner.
     *
     * @return summary of assignments (orderId -> partnerId)
     */
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<Map<Long, Long>>> assignPartners()
            throws ExecutionException, InterruptedException {

        Map<Long, Long> assignments = deliveryAssignmentService.optimizeAssignmentsForReadyOrders();
        return ResponseEntity.ok(ApiResponse.success("Assignment completed", assignments));
    }
}



