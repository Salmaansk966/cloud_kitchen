package com.cloudkitchen.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistoryItemDto {

    private Long orderId;
    private Double totalAmount;
    private String status;
    private OffsetDateTime createdAt;
}





