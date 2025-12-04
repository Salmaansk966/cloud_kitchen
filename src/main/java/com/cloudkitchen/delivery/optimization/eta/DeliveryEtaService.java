package com.cloudkitchen.delivery.optimization.eta;

import com.cloudkitchen.entity.DeliveryPartner;
import com.cloudkitchen.entity.Order;
import com.cloudkitchen.entity.enums.OrderStatus;
import com.cloudkitchen.exception.ResourceNotFoundException;
import com.cloudkitchen.repository.DeliveryPartnerRepository;
import com.cloudkitchen.repository.OrderRepository;
import com.cloudkitchen.util.HaversineUtil;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * Service for predicting delivery ETAs using simple heuristics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryEtaService {

    private final OrderRepository orderRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final DeliveryEtaProperties properties;

    /**
     * Predict ETA for an existing order by ID.
     */
    public EtaResult predictEtaForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));

        DeliveryPartner partner = order.getPartner();
        if (partner == null) {
            throw new IllegalStateException("Order has no assigned delivery partner.");
        }

        return predictEta(order, partner);
    }

    /**
     * Core ETA calculation logic based on order, partner and distance.
     */
    private EtaResult predictEta(Order order, DeliveryPartner partner) {
        int prepMinutes = computePrepMinutes(order);

        double distanceKm = computeDistanceKm(order, partner);

        double travelMinutes = computeTravelMinutes(distanceKm);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime estimatedPickupTime;

        if (order.getStatus().ordinal() < OrderStatus.READY.ordinal()) {
            estimatedPickupTime = now.plusMinutes(prepMinutes);
        } else if (order.getPickedUpAt() != null) {
            estimatedPickupTime = order.getPickedUpAt();
        } else {
            estimatedPickupTime = now;
        }

        OffsetDateTime estimatedDeliveryTime = estimatedPickupTime
                .plusMinutes(Math.round(travelMinutes));

        return new EtaResult(
                order.getId(),
                estimatedPickupTime,
                estimatedDeliveryTime,
                prepMinutes,
                travelMinutes,
                distanceKm
        );
    }

    private int computePrepMinutes(Order order) {
        if (order.getStatus().ordinal() >= OrderStatus.READY.ordinal()) {
            return 0;
        }
        // For now fall back to default; can be extended with per-kitchen statistics.
        return properties.getDefaultKitchenPrepMinutes();
    }

    private double computeDistanceKm(Order order, DeliveryPartner partner) {
        if (order.getDeliveryLat() == null || order.getDeliveryLng() == null
                || partner.getCurrentLat() == null || partner.getCurrentLng() == null) {
            return 0.0;
        }

        return HaversineUtil.distanceInKm(
                partner.getCurrentLat(), partner.getCurrentLng(),
                order.getDeliveryLat(), order.getDeliveryLng()
        );
    }

    private double computeTravelMinutes(double distanceKm) {
        if (distanceKm <= 0.0) {
            return 0.0;
        }

        double speed = properties.getDefaultAverageSpeedKmph();
        if (speed <= 0.0) {
            speed = 18.0;
        }

        double baseMinutes = (distanceKm / speed) * 60.0;
        return baseMinutes * properties.getTrafficSlowdownFactor();
    }

    /**
     * Immutable DTO for returning ETA information.
     */
    @Value
    public static class EtaResult {
        Long orderId;
        OffsetDateTime estimatedPickupTime;
        OffsetDateTime estimatedDeliveryTime;
        int prepMinutes;
        double travelMinutes;
        double distanceKm;
    }
}



