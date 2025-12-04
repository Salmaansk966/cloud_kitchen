package com.cloudkitchen.delivery.optimization.eta;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for ETA calculation.
 */
@Component
@ConfigurationProperties(prefix = "delivery.eta")
@Getter
@Setter
public class DeliveryEtaProperties {

    /**
     * Default kitchen preparation time in minutes when historical data is not available.
     */
    private int defaultKitchenPrepMinutes = 15;

    /**
     * Default average speed in km/h used to estimate travel time.
     */
    private double defaultAverageSpeedKmph = 18.0;

    /**
     * Factor applied to travel time to account for traffic and delays.
     */
    private double trafficSlowdownFactor = 1.2;
}



