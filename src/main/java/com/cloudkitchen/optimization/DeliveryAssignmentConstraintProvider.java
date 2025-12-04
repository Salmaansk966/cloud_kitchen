package com.cloudkitchen.optimization;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.cloudkitchen.entity.enums.PartnerStatus;
import com.cloudkitchen.optimization.domain.DeliveryAssignment;
import com.cloudkitchen.util.HaversineUtil;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import static com.cloudkitchen.optimization.DeliveryAssignmentConstants.MAX_DELIVERY_RADIUS_KM;

public class DeliveryAssignmentConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
                partnerMustBeAssigned(factory),
                partnerMustBeOnline(factory),
                partnerWithinDeliveryRadius(factory),
                minimizeDistanceToPickup(factory),
                balancePartnerWorkload(factory)
        };
    }

    private Constraint partnerMustBeAssigned(ConstraintFactory factory) {
        return factory.forEach(DeliveryAssignment.class)
                .filter(a -> a.getPartner() == null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Unassigned order");
    }

    private Constraint partnerMustBeOnline(ConstraintFactory factory) {
        return factory.forEach(DeliveryAssignment.class)
                .filter(a -> a.getPartner() != null
                        && a.getPartner().getStatus() != PartnerStatus.ONLINE)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Partner must be online");
    }

    private Constraint partnerWithinDeliveryRadius(ConstraintFactory factory) {
        return factory.forEach(DeliveryAssignment.class)
                .filter(a -> a.getPartner() != null
                        && a.getPickupLat() != null
                        && a.getPickupLng() != null
                        && a.getPartner().getCurrentLat() != null
                        && a.getPartner().getCurrentLng() != null)
                .filter(a -> {
                    double distance = HaversineUtil.distanceInKm(
                            a.getPickupLat(), a.getPickupLng(),
                            a.getPartner().getCurrentLat(), a.getPartner().getCurrentLng());
                    return distance > MAX_DELIVERY_RADIUS_KM;
                })
                .penalize(HardSoftScore.ofHard((int) 1000.0))
                .asConstraint("Partner outside delivery radius");
    }

    private Constraint minimizeDistanceToPickup(ConstraintFactory factory) {
        return factory.forEach(DeliveryAssignment.class)
                .filter(a -> a.getPartner() != null
                        && a.getPickupLat() != null
                        && a.getPickupLng() != null
                        && a.getPartner().getCurrentLat() != null
                        && a.getPartner().getCurrentLng() != null)
                .penalize(HardSoftScore.ONE_SOFT,
                        a -> (int) Math.round(
                                HaversineUtil.distanceInKm(
                                        a.getPickupLat(), a.getPickupLng(),
                                        a.getPartner().getCurrentLat(), a.getPartner().getCurrentLng()
                                ) * 1000.0))
                .asConstraint("Minimize distance to pickup");
    }

    private Constraint balancePartnerWorkload(ConstraintFactory factory) {
        return factory.forEach(DeliveryAssignment.class)
                .filter(a -> a.getPartner() != null)
                .groupBy(DeliveryAssignment::getPartner, ConstraintCollectors.count())
                .penalize(HardSoftScore.ONE_SOFT,
                        (partner, count) -> count * count)
                .asConstraint("Balance partner workload");
    }
}


