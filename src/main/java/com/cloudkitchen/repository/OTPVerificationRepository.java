package com.cloudkitchen.repository;

import com.cloudkitchen.entity.OTPVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OTPVerificationRepository extends JpaRepository<OTPVerification, Long> {

    Optional<OTPVerification> findByOrderIdAndOtpAndUsedFalse(Long orderId, String otp);
}





