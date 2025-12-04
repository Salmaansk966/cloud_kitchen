package com.cloudkitchen.delivery.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.cloudkitchen.delivery.domain.location.Location;
import com.cloudkitchen.delivery.optimization.routing.DeliveryVehicle;
import com.cloudkitchen.delivery.optimization.routing.OrderStop;
import com.cloudkitchen.delivery.optimization.routing.RoutePlanSolution;
import com.cloudkitchen.entity.DeliveryPartner;
import com.cloudkitchen.entity.Order;
import com.cloudkitchen.entity.enums.OrderStatus;
import com.cloudkitchen.repository.DeliveryPartnerRepository;
import com.cloudkitchen.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Service that builds and solves route optimization problems for
 * delivery partners using Timefold.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteOptimizationService {

    private final SolverManager<RoutePlanSolution, Long> routeSolverManager;
    private final DeliveryPartnerRepository partnerRepository;
    private final OrderRepository orderRepository;

    /**
     * Plan routes for a single partner based on their assigned orders
     * that are ready to be picked up.
     */
    @Transactional
    public List<RouteStopDto> planRoutesForPartner(Long partnerId)
            throws ExecutionException, InterruptedException {

        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new IllegalArgumentException("Partner not found: " + partnerId));

        List<Order> orders = orderRepository.findByStatus(OrderStatus.PICK_UP)
                .stream()
                .filter(o -> o.getPartner() != null && o.getPartner().getId().equals(partnerId))
                .toList();

        if (orders.isEmpty()) {
            log.info("No PICK_UP orders for partner {} to optimize routes.", partnerId);
            return Collections.emptyList();
        }

        DeliveryVehicle vehicle = DeliveryVehicle.builder()
                .id(partnerId)
                .partner(partner)
                .startLocation(new Location(
                        Optional.ofNullable(partner.getCurrentLat()).orElse(0.0),
                        Optional.ofNullable(partner.getCurrentLng()).orElse(0.0)))
                .capacity(orders.size())
                .build();

        List<OrderStop> stops = orders.stream()
                .map(o -> OrderStop.builder()
                        .orderId(o.getId())
                        .location(o.getDeliveryLat() != null && o.getDeliveryLng() != null
                                ? new Location(o.getDeliveryLat(), o.getDeliveryLng())
                                : null)
                        .build())
                .toList();

        RoutePlanSolution problem = RoutePlanSolution.builder()
                .vehicleList(List.of(vehicle))
                .visitList(stops)
                .build();

        long problemId = partnerId;
        log.info("Starting route optimization for partner {} with {} stops.", partnerId, stops.size());

        RoutePlanSolution solution = routeSolverManager
                .solve(problemId, problem)
                .getFinalBestSolution();

        log.info("Route optimization finished for partner {} with score {}", partnerId, solution.getScore());

        return extractOrderedStops(solution, vehicle);
    }

    /**
     * Extract ordered stops following the chained previousStandstill pointers.
     */
    private List<RouteStopDto> extractOrderedStops(RoutePlanSolution solution, DeliveryVehicle vehicle) {
        List<RouteStopDto> ordered = new ArrayList<>();

        // Build map from previous standstill to next stop (successor map).
        Map<Object, OrderStop> nextByPrev = new HashMap<>();
        for (OrderStop stop : solution.getVisitList()) {
            if (stop.getPreviousStandstill() != null) {
                nextByPrev.put(stop.getPreviousStandstill(), stop);
            }
        }

        Object current = vehicle;
        int sequence = 1;
        while (nextByPrev.containsKey(current)) {
            OrderStop stop = nextByPrev.get(current);
            ordered.add(new RouteStopDto(
                    stop.getOrderId(),
                    sequence++,
                    stop.getLocation() != null ? stop.getLocation().getLatitude() : null,
                    stop.getLocation() != null ? stop.getLocation().getLongitude() : null));
            current = stop;
        }

        return ordered;
    }

    /**
     * Simple DTO representing an ordered stop for a partner.
     */
    public record RouteStopDto(Long orderId, int sequence, Double lat, Double lng) {
    }
}



