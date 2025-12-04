package com.cloudkitchen.delivery.tracking;

import com.cloudkitchen.entity.DeliveryPartner;
import com.cloudkitchen.dto.tracking.OrderRouteInfoDto;
import com.cloudkitchen.entity.LocationTracking;
import com.cloudkitchen.entity.Order;
import com.cloudkitchen.entity.enums.OrderStatus;
import com.cloudkitchen.repository.DeliveryPartnerRepository;
import com.cloudkitchen.repository.LocationTrackingRepository;
import com.cloudkitchen.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling live tracking updates: persisting history and
 * broadcasting partner locations to customers subscribed per order.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryTrackingService {

    private final DeliveryPartnerRepository partnerRepository;
    private final OrderRepository orderRepository;
    private final LocationTrackingRepository locationTrackingRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Update partner location, persist a history entry, and broadcast
     * the new position to all active orders for that partner.
     */
    @Transactional
    public void handlePartnerLocationUpdate(PartnerLocationUpdateRequest request) {
        DeliveryPartner partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new IllegalArgumentException("Partner not found: " + request.getPartnerId()));

        partner.setCurrentLat(request.getLatitude());
        partner.setCurrentLng(request.getLongitude());
        partnerRepository.save(partner);

        LocationTracking tracking = LocationTracking.builder()
                .partner(partner)
                .lat(request.getLatitude())
                .lng(request.getLongitude())
                .timestamp(OffsetDateTime.now())
                .build();
        locationTrackingRepository.save(tracking);

        // Broadcast to customers for each active order of this partner.
        List<Order> activeOrders = orderRepository.findByStatus(OrderStatus.PICK_UP)
                .stream()
                .filter(o -> o.getPartner() != null && o.getPartner().getId().equals(partner.getId()))
                .toList();

        for (Order order : activeOrders) {
            OrderLocationUpdateEvent event = new OrderLocationUpdateEvent(
                    order.getId(),
                    partner.getId(),
                    request.getLatitude(),
                    request.getLongitude(),
                    tracking.getTimestamp()
            );
            String destination = "/topic/order/" + order.getId() + "/location";
            messagingTemplate.convertAndSend(destination, event);
            log.info("Broadcasted location for order {} to {}", order.getId(), destination);
        }
    }

    /**
     * Fetch the latest available partner coordinates along with the customer's
     * delivery coordinates so the UI can draw the optimal path.
     */
    @Transactional(readOnly = true)
    public OrderRouteInfoDto getOrderRouteInfo(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getDeliveryLat() == null || order.getDeliveryLng() == null) {
            throw new IllegalStateException("Order does not have delivery coordinates");
        }

        DeliveryPartner partner = Optional.ofNullable(order.getPartner())
                .orElseThrow(() -> new IllegalStateException("Order has no assigned partner"));

        Double partnerLat = partner.getCurrentLat();
        Double partnerLng = partner.getCurrentLng();

        if (partnerLat == null || partnerLng == null) {
            locationTrackingRepository.findTopByPartnerIdOrderByTimestampDesc(partner.getId())
                    .ifPresent((LocationTracking tracking) -> {
                        partner.setCurrentLat(tracking.getLat());
                        partner.setCurrentLng(tracking.getLng());
                    });
            partnerLat = partner.getCurrentLat();
            partnerLng = partner.getCurrentLng();
        }

        if (partnerLat == null || partnerLng == null) {
            throw new IllegalStateException("No recent location data for partner " + partner.getId());
        }

        return OrderRouteInfoDto.builder()
                .orderId(order.getId())
                .partnerId(partner.getId())
                .partnerLat(partnerLat)
                .partnerLng(partnerLng)
                .deliveryLat(order.getDeliveryLat())
                .deliveryLng(order.getDeliveryLng())
                .build();
    }
}



