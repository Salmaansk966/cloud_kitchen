package com.cloudkitchen.delivery.config;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.solver.SolverConfig;
import com.cloudkitchen.delivery.optimization.assignment.AssignmentConstraintProvider;
import com.cloudkitchen.delivery.optimization.assignment.AssignmentSolution;
import com.cloudkitchen.delivery.optimization.assignment.OrderAssignment;
import com.cloudkitchen.delivery.optimization.routing.OrderStop;
import com.cloudkitchen.delivery.optimization.routing.RouteConstraintProvider;
import com.cloudkitchen.delivery.optimization.routing.RoutePlanSolution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Timefold solver configuration for delivery optimization.
 * <p>
 * Exposes {@link SolverManager} beans for the assignment and routing
 * solutions so that services can trigger optimization runs.
 */
@Configuration
public class DeliveryOptimizationSolverConfig {

    @Bean
    public SolverFactory<AssignmentSolution> assignmentSolverFactory() {
        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(AssignmentSolution.class)
                .withEntityClasses(OrderAssignment.class)
                .withConstraintProviderClass(AssignmentConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(2));
        return SolverFactory.create(solverConfig);
    }

    @Bean
    public SolverManager<AssignmentSolution, Long> assignmentSolverManager(
            SolverFactory<AssignmentSolution> assignmentSolverFactory) {
        return SolverManager.create(assignmentSolverFactory);
    }

    @Bean
    public SolverFactory<RoutePlanSolution> routeSolverFactory() {
        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(RoutePlanSolution.class)
                .withEntityClasses(OrderStop.class)
                .withConstraintProviderClass(RouteConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(2));
        return SolverFactory.create(solverConfig);
    }

    @Bean
    public SolverManager<RoutePlanSolution, Long> routeSolverManager(
            SolverFactory<RoutePlanSolution> routeSolverFactory) {
        return SolverManager.create(routeSolverFactory);
    }
}



