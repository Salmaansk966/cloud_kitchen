package com.cloudkitchen.service;

import com.cloudkitchen.dto.tracking.LocationUpdateDto;
import com.cloudkitchen.dto.tracking.PartnerLocationDto;
import org.springframework.security.core.Authentication;

public interface TrackingService {

    void updatePartnerLocation(Authentication auth, LocationUpdateDto request);

    Long findNearestPartner(Double lat, Double lng);
}





