package com.cloudkitchen.service;

import com.cloudkitchen.dto.partner.PartnerEarningsDto;
import com.cloudkitchen.dto.partner.PartnerStatusUpdateDto;
import org.springframework.security.core.Authentication;

public interface PartnerService {

    void updateOnlineStatus(Authentication auth, PartnerStatusUpdateDto request);

    PartnerEarningsDto getEarnings(Authentication auth);
}





