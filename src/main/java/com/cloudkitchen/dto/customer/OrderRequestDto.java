package com.cloudkitchen.dto.customer;

import com.cloudkitchen.entity.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private Long menuItemId;
        private Integer quantity;
    }

    private List<Item> items;
    private PaymentMethod paymentMethod;
    private String deliveryAddress;
    private Double deliveryLat;
    private Double deliveryLng;
}





