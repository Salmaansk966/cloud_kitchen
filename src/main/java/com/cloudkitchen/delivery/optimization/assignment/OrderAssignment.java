package com.cloudkitchen.delivery.optimization.assignment;

import com.cloudkitchen.delivery.domain.location.Location;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PlanningEntity: represents a single order that must be assigned
 * to a delivery partner.
 */
@PlanningEntity
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAssignment {

    /**
     * Business identifier of the order in the database.
     */
    private Long orderId;

    /**
     * Location of the order (customer location).
     */
    private Location orderLocation;

    /**
     * PlanningVariable: which partner is assigned to deliver this order.
     * <p>
     * The available values come from the "partnerRange" defined
     * on the {@link AssignmentSolution} partner list.
     */
    @PlanningVariable(valueRangeProviderRefs = "partnerRange")
    private DeliveryPartnerFact assignedPartner;

    // Explicit getters so code using method references works even when
    // Lombok annotation processing is limited in some tooling.

    public Long getOrderId() {
        return orderId;
    }

    public Location getOrderLocation() {
        return orderLocation;
    }

    public DeliveryPartnerFact getAssignedPartner() {
        return assignedPartner;
    }
}

