package com.cloudkitchen.delivery.optimization.routing;

import com.cloudkitchen.delivery.domain.location.Location;

/**
 * Common interface for all points a route can stand still at:
 * vehicle starting positions and order stops.
 */
public interface Standstill {

    Location getLocation();
}



