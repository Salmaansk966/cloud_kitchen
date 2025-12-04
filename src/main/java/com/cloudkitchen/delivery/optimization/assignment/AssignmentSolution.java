package com.cloudkitchen.delivery.optimization.assignment;

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

import java.util.List;

/**
 * PlanningSolution for nearest delivery partner assignment.
 * <p>
 * Contains all problem facts (partners) and planning entities (orders)
 * plus the resulting score.
 */
@PlanningSolution
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentSolution {

    /**
     * Problem facts: all candidate partners.
     * Also acts as the value range for the planning variable.
     */
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "partnerRange")
    private List<DeliveryPartnerFact> partnerList;

    /**
     * Planning entities: all orders that must be assigned.
     */
    @PlanningEntityCollectionProperty
    private List<OrderAssignment> assignmentList;

    /**
     * Combined hard/soft score:
     * hard = constraint feasibility,
     * soft = distance and workload balance.
     */
    @PlanningScore
    private HardSoftScore score;
}



