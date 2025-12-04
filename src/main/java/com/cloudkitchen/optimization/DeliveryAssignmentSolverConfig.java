package com.cloudkitchen.optimization;

import com.cloudkitchen.optimization.domain.DeliveryAssignment;
import com.cloudkitchen.optimization.domain.DeliveryAssignmentSolution;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.solver.SolverConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class DeliveryAssignmentSolverConfig {

    @Bean
    public SolverFactory<DeliveryAssignmentSolution> deliveryAssignmentSolverFactory() {
        SolverConfig solverConfig = new SolverConfig()
                .withTerminationSpentLimit(Duration.ofMillis(200))
                .withSolutionClass(DeliveryAssignmentSolution.class)
                .withEntityClasses(DeliveryAssignment.class)
                .withConstraintProviderClass(DeliveryAssignmentConstraintProvider.class);
        return SolverFactory.create(solverConfig);
    }

    @Bean
    public SolverManager<DeliveryAssignmentSolution, String> deliveryAssignmentSolverManager(
            SolverFactory<DeliveryAssignmentSolution> solverFactory) {
        return SolverManager.create(solverFactory);
    }
}





