package com.cloudkitchen.delivery.optimization.routing;

import com.cloudkitchen.delivery.domain.location.Location;
import com.cloudkitchen.entity.DeliveryPartner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Problem fact representing a single delivery partner's vehicle/trip.
 * Acts as the anchor (start) of a route and implements Standstill.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryVehicle implements Standstill {

    private Long id;

    private DeliveryPartner partner;

    private Location startLocation;

    /** Maximum number of orders per batch/route. */
    private int capacity;

    @Override
    public Location getLocation() {
        return startLocation;
    }
}



