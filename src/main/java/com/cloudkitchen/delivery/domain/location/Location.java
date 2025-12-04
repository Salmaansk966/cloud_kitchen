package com.cloudkitchen.delivery.domain.location;

import lombok.Value;

/**
 * Simple immutable geographic location value object.
 * <p>
 * Provides a Haversine-based distance calculation in meters.
 */
@Value
public class Location {

    double latitude;
    double longitude;

    /**
     * Calculates the distance from this location to another using the Haversine formula.
     *
     * @param other target location (must not be {@code null})
     * @return distance in meters
     */
    public double distanceTo(Location other) {
        final double R = 6_371_000.0; // Earth radius in meters

        double latRad1 = Math.toRadians(latitude);
        double latRad2 = Math.toRadians(other.latitude);
        double dLat = Math.toRadians(other.latitude - latitude);
        double dLon = Math.toRadians(other.longitude - longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(latRad1) * Math.cos(latRad2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}



