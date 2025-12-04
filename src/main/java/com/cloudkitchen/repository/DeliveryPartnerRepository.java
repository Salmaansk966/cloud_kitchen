package com.cloudkitchen.repository;

import com.cloudkitchen.entity.DeliveryPartner;
import com.cloudkitchen.entity.enums.PartnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {

    List<DeliveryPartner> findByStatus(PartnerStatus status);

    Optional<DeliveryPartner> findByUserEmail(String email);
}





