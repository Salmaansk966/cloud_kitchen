package com.cloudkitchen.dto.partner;

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
public class PartnerEarningsDto {

    private Double totalEarnings;
    private Integer completedOrders;
    private Double todayEarnings;
}





