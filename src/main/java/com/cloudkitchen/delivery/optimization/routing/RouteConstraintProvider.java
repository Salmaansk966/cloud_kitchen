package com.cloudkitchen.delivery.optimization.routing;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import org.springframework.lang.NonNull;

/**
 * ConstraintProvider for route planning.
 * <p>
 * Hard constraints:
 * - vehicle capacity
 * - all visits must be assigned to a vehicle
 * Soft constraints:
 * - minimize total travel distance.
 */
public class RouteConstraintProvider implements ConstraintProvider {

    @Override
    @NonNull
    public Constraint[] defineConstraints(@NonNull ConstraintFactory factory) {
        return new Constraint[]{
                capacityConstraint(factory),
                allVisitsAssigned(factory),
                minimizeTravelDistance(factory)
        };
    }

    /** HARD: number of visits per vehicle must not exceed capacity. */
    private Constraint capacityConstraint(ConstraintFactory factory) {
        return factory.forEach(OrderStop.class)
                .filter(stop -> stop.getVehicle() != null)
                .groupBy(OrderStop::getVehicle, ConstraintCollectors.count())
                .filter((vehicle, count) -> count > vehicle.getCapacity())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Vehicle capacity");
    }

    /** HARD: each visit must be assigned to exactly one vehicle. */
    private Constraint allVisitsAssigned(ConstraintFactory factory) {
        return factory.forEach(OrderStop.class)
                .filter(stop -> stop.getVehicle() == null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("All visits assigned");
    }

    /**
     * SOFT: minimize travel distance between previous standstill and visit.
     */
    private Constraint minimizeTravelDistance(ConstraintFactory factory) {
        return factory.forEach(OrderStop.class)
                .filter(stop -> stop.getPreviousStandstill() != null
                        && stop.getPreviousStandstill().getLocation() != null
                        && stop.getLocation() != null)
                .penalize(HardSoftScore.ONE_SOFT,
                        stop -> (int) Math.round(
                                stop.getPreviousStandstill().getLocation()
                                        .distanceTo(stop.getLocation()) / 10.0))
                .asConstraint("Minimize travel distance");
    }
}



