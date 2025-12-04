package com.cloudkitchen.delivery.optimization.assignment;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import com.cloudkitchen.delivery.domain.location.Location;
import org.springframework.lang.NonNull;

/**
 * ConstraintProvider for nearest delivery partner assignment.
 * <p>
 * Defines hard constraints for feasibility and soft constraints for
 * optimizing distance and load balancing.
 */
public class AssignmentConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(@NonNull ConstraintFactory factory) {
        return new Constraint[]{
                noOfflinePartner(factory),
                respectCapacity(factory),
                minimizeDistance(factory),
                balanceWorkload(factory)
        };
    }

    /**
     * HARD: orders must not be assigned to offline partners.
     */
    private Constraint noOfflinePartner(ConstraintFactory factory) {
        return factory.forEach(OrderAssignment.class)
                .filter(a -> a.getAssignedPartner() != null && !a.getAssignedPartner().isOnline())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("No offline partner");
    }

    /**
     * HARD: respect partner capacity (currentLoad + newAssignments <= maxCapacity).
     */
    private Constraint respectCapacity(ConstraintFactory factory) {
        return factory.forEach(OrderAssignment.class)
                .filter(a -> a.getAssignedPartner() != null)
                .groupBy(OrderAssignment::getAssignedPartner, ConstraintCollectors.count())
                .filter((partner, newAssignments) ->
                        partner.getCurrentLoad() + newAssignments > partner.getMaxCapacity())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Respect partner capacity");
    }

    /**
     * SOFT: minimize travel distance from partner to order.
     */
    private Constraint minimizeDistance(ConstraintFactory factory) {
        return factory.forEach(OrderAssignment.class)
                .filter(a -> a.getAssignedPartner() != null
                        && a.getOrderLocation() != null
                        && a.getAssignedPartner().getCurrentLocation() != null)
                .penalize(HardSoftScore.ONE_SOFT,
                        a -> (int) Math.round(distanceMeters(
                                a.getAssignedPartner().getCurrentLocation(),
                                a.getOrderLocation()) / 10.0))
                .asConstraint("Minimize distance");
    }

    /**
     * SOFT: balance workload by penalizing partners with very high load.
     */
    private Constraint balanceWorkload(ConstraintFactory factory) {
        return factory.forEach(OrderAssignment.class)
                .filter(a -> a.getAssignedPartner() != null)
                .groupBy(OrderAssignment::getAssignedPartner, ConstraintCollectors.count())
                .penalize(HardSoftScore.ONE_SOFT,
                        (partner, newAssignments) -> {
                            int total = partner.getCurrentLoad() + newAssignments;
                            return total * total;
                        })
                .asConstraint("Balance workload");
    }

    private double distanceMeters(Location from, Location to) {
        return from.distanceTo(to);
    }
}


