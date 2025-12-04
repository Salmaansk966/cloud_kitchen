package com.cloudkitchen.delivery.optimization.assignment;

import com.cloudkitchen.delivery.domain.location.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Problem fact representing a delivery partner in the optimization problem.
 * <p>
 * This is immutable from the solver's perspective (no Timefold annotations).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartnerFact {

    private Long id;

    private Location currentLocation;

    private boolean online;

    /**
     * Current active load (number of orders).
     */
    private int currentLoad;

    /**
     * Maximum allowed concurrent orders.
     */
    private int maxCapacity;

    // Explicit getters so constraint streams work even if Lombok
    // annotation processing is limited in some tooling.

    public Long getId() {
        return id;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public boolean isOnline() {
        return online;
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
}

