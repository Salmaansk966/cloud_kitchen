package com.cloudkitchen.delivery.tracking;

import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for partner location updates sent from the partner app.
 */
@Getter
@Setter
public class PartnerLocationUpdateRequest {

    private Long partnerId;
    private Double latitude;
    private Double longitude;
}



