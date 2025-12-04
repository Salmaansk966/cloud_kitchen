package com.cloudkitchen.optimization.domain;

import com.cloudkitchen.entity.DeliveryPartner;
import com.cloudkitchen.entity.Order;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PlanningEntity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAssignment {

    @PlanningId
    private Long id;

    /** Problem fact: order that needs to be delivered. */
    private Order order;

    /** Planning variable: which partner will deliver this order. */
    @PlanningVariable(valueRangeProviderRefs = "partnerRange")
    private DeliveryPartner partner;

    public Long getOrderId() {
        return order != null ? order.getId() : null;
    }

    public Double getPickupLat() {
        return order != null ? order.getDeliveryLat() : null;
    }

    public Double getPickupLng() {
        return order != null ? order.getDeliveryLng() : null;
    }
}





