package com.cloudkitchen.service.impl;

import com.cloudkitchen.dto.tracking.LocationUpdateDto;
import com.cloudkitchen.service.TrackingService;
import com.cloudkitchen.entity.DeliveryPartner;
import com.cloudkitchen.repository.DeliveryPartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackingServiceImpl implements TrackingService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;

    @Override
    public void updatePartnerLocation(Authentication auth, LocationUpdateDto request) {
        // get partner by login
        String email = auth.getName();

        DeliveryPartner partner = deliveryPartnerRepository
                .findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        partner.setCurrentLat(request.getLat());
        partner.setCurrentLng(request.getLng());

        deliveryPartnerRepository.save(partner);
    }

    @Override
    public Long findNearestPartner(Double lat, Double lng) {
        return deliveryPartnerRepository.findAll().stream()
                .filter(p -> p.getCurrentLat() != null && p.getCurrentLng() != null)
                .min((p1, p2) -> {
                    double d1 = distance(lat, lng, p1.getCurrentLat(), p1.getCurrentLng());
                    double d2 = distance(lat, lng, p2.getCurrentLat(), p2.getCurrentLng());
                    return Double.compare(d1, d2);
                })
                .map(DeliveryPartner::getId)
                .orElse(null);
    }

    private double distance(double lat1, double lng1, double lat2, double lng2) {
        double dx = lat1 - lat2;
        double dy = lng1 - lng2;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
