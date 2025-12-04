package com.cloudkitchen.optimization.domain;

import com.cloudkitchen.entity.DeliveryPartner;
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

@PlanningSolution
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAssignmentSolution {

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "partnerRange")
    private List<DeliveryPartner> partnerList;

    @PlanningEntityCollectionProperty
    private List<DeliveryAssignment> assignmentList;

    @PlanningScore
    private HardSoftScore score;
}


