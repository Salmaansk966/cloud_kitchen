package com.cloudkitchen.delivery.optimization.routing;

import com.cloudkitchen.delivery.domain.location.Location;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariableGraphType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PlanningEntity representing a visit (customer order) in a route.
 * <p>
 * Uses a chained variable {@code previousStandstill} to model the order
 * of visits for a vehicle route.
 */
@PlanningEntity
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStop implements Standstill {

    private Long orderId;

    private Location location;

    /**
     * PlanningVariable: which vehicle this stop belongs to.
     * Nullable so that the solver can decide assignment; hard constraints
     * will penalize unassigned stops.
     */
    @PlanningVariable(valueRangeProviderRefs = "vehicleRange", nullable = true)
    private DeliveryVehicle vehicle;

    /**
     * Chained PlanningVariable: previous standstill in the route.
     * <p>
     * Models Vehicle (start) -> OrderStop -> OrderStop -> null.
     */
    @PlanningVariable(graphType = PlanningVariableGraphType.CHAINED,
            valueRangeProviderRefs = "standstillRange")
    private Standstill previousStandstill;

    public Long getOrderId() {
        return orderId;
    }

    public Location getLocation() {
        return location;
    }

    public DeliveryVehicle getVehicle() {
        return vehicle;
    }

    public Standstill getPreviousStandstill() {
        return previousStandstill;
    }
}



