package com.cloudkitchen.repository;

import com.cloudkitchen.entity.LocationTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationTrackingRepository extends JpaRepository<LocationTracking, Long> {

    List<LocationTracking> findTop10ByPartnerIdOrderByTimestampDesc(Long partnerId);

    Optional<LocationTracking> findTopByPartnerIdOrderByTimestampDesc(Long partnerId);
}





