package com.cloudkitchen.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerLocationDto {

    private Long partnerId;
    private Double lat;
    private Double lng;
}





