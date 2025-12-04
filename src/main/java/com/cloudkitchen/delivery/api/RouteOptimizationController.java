package com.cloudkitchen.delivery.api;

import com.cloudkitchen.delivery.service.RouteOptimizationService;
import com.cloudkitchen.delivery.service.RouteOptimizationService.RouteStopDto;
import com.cloudkitchen.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * REST API for multi-order route optimization.
 */
@RestController
@RequestMapping("/api/optimizer/routes")
@RequiredArgsConstructor
public class RouteOptimizationController {

    private final RouteOptimizationService routeOptimizationService;

    /**
     * Optimize route for a single partner and return the ordered stops.
     */
    @PostMapping("/partner/{partnerId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> optimizeForPartner(
            @PathVariable Long partnerId) throws ExecutionException, InterruptedException {

        List<RouteStopDto> stops = routeOptimizationService.planRoutesForPartner(partnerId);

        Map<String, Object> data = new HashMap<>();
        data.put("partnerId", partnerId);
        data.put("stops", stops);

        return ResponseEntity.ok(ApiResponse.success("Route planned", data));
    }
}



