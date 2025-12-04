package com.cloudkitchen.delivery.optimization.routing;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * PlanningSolution for multi-order route optimization (VRP-like).
 */
@PlanningSolution
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutePlanSolution {

    /**
     * Problem facts: vehicles (delivery partners' trips).
     */
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "vehicleRange")
    private List<DeliveryVehicle> vehicleList;

    /**
     * All visits (orders) that must be placed on routes.
     */
    @PlanningEntityCollectionProperty
    private List<OrderStop> visitList;

    /**
     * Combined value range for chained variable:
     * all possible previous standstills = vehicles + visits.
     */
    @ValueRangeProvider(id = "standstillRange")
    public List<Standstill> getStandstillRange() {
        List<Standstill> range = new ArrayList<>();
        if (vehicleList != null) {
            range.addAll(vehicleList);
        }
        if (visitList != null) {
            range.addAll(visitList);
        }
        return range;
    }

    @PlanningScore
    private HardSoftScore score;
}



