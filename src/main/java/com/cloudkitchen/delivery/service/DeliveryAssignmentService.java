package com.cloudkitchen.delivery.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.cloudkitchen.delivery.domain.location.Location;
import com.cloudkitchen.delivery.optimization.assignment.AssignmentSolution;
import com.cloudkitchen.delivery.optimization.assignment.DeliveryPartnerFact;
import com.cloudkitchen.delivery.optimization.assignment.OrderAssignment;
import com.cloudkitchen.entity.DeliveryPartner;
import com.cloudkitchen.entity.Order;
import com.cloudkitchen.entity.enums.OrderStatus;
import com.cloudkitchen.entity.enums.PartnerStatus;
import com.cloudkitchen.repository.DeliveryPartnerRepository;
import com.cloudkitchen.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Service that builds a Timefold assignment problem from READY orders
 * and online partners, runs the solver, and applies assignments back
 * to the Order entities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryAssignmentService {

    private final SolverManager<AssignmentSolution, Long> assignmentSolverManager;
    private final OrderRepository orderRepository;
    private final DeliveryPartnerRepository partnerRepository;

    /**
     * Builds the assignment problem instance from current READY orders
     * and available partners.
     */
    public AssignmentSolution buildProblemForReadyOrders() {
        List<Order> readyOrders = orderRepository.findByStatus(OrderStatus.READY)
                .stream()
                .filter(o -> o.getPartner() == null)
                .toList();

        List<DeliveryPartner> partners = partnerRepository.findByStatus(PartnerStatus.ONLINE);

        if (readyOrders.isEmpty() || partners.isEmpty()) {
            log.info("No ready orders ({}) or online partners ({}) to optimize.", readyOrders.size(), partners.size());
        } else {
            log.info("Building assignment problem: {} ready orders, {} partners.", readyOrders.size(), partners.size());
        }

        Map<Long, DeliveryPartner> partnerById = partners.stream()
                .collect(Collectors.toMap(DeliveryPartner::getId, p -> p));

        // For now, currentLoad is approximated as 0 and maxCapacity as a small constant.
        // This can be improved by counting active orders and/or adding fields to DeliveryPartner.
        List<DeliveryPartnerFact> partnerFacts = partners.stream()
                .map(p -> DeliveryPartnerFact.builder()
                        .id(p.getId())
                        .currentLocation(new Location(
                                Optional.ofNullable(p.getCurrentLat()).orElse(0.0),
                                Optional.ofNullable(p.getCurrentLng()).orElse(0.0)))
                        .online(p.getStatus() == PartnerStatus.ONLINE && p.isActive())
                        .currentLoad(0)
                        .maxCapacity(3)
                        .build())
                .collect(Collectors.toList());

        List<OrderAssignment> assignments = readyOrders.stream()
                .map(o -> OrderAssignment.builder()
                        .orderId(o.getId())
                        .orderLocation(o.getDeliveryLat() != null && o.getDeliveryLng() != null
                                ? new Location(o.getDeliveryLat(), o.getDeliveryLng())
                                : null)
                        .build())
                .collect(Collectors.toList());

        return AssignmentSolution.builder()
                .partnerList(partnerFacts)
                .assignmentList(assignments)
                .build();
    }

    /**
     * Run the solver synchronously and apply assignments back to the database.
     *
     * @return map of orderId -> partnerId that were assigned
     */
    @Transactional
    public Map<Long, Long> runSolverAndApplyAssignments() throws ExecutionException, InterruptedException {
        AssignmentSolution problem = buildProblemForReadyOrders();

        if (problem.getAssignmentList() == null || problem.getAssignmentList().isEmpty()
                || problem.getPartnerList() == null || problem.getPartnerList().isEmpty()) {
            return Collections.emptyMap();
        }

        long problemId = 1L;
        log.info("Starting Timefold assignment solver for problemId={}", problemId);

        AssignmentSolution solution = assignmentSolverManager
                .solve(problemId, problem)
                .getFinalBestSolution();

        log.info("Assignment solver finished with score: {}", solution.getScore());

        Map<Long, Long> result = new HashMap<>();

        // Load fresh entities for update
        Map<Long, DeliveryPartner> partnerById = partnerRepository.findAll().stream()
                .collect(Collectors.toMap(DeliveryPartner::getId, p -> p));

        for (OrderAssignment assignment : solution.getAssignmentList()) {
            DeliveryPartnerFact partnerFact = assignment.getAssignedPartner();
            if (partnerFact == null) {
                continue;
            }

            Long orderId = assignment.getOrderId();
            Long partnerId = partnerFact.getId();

            Order order = orderRepository.findById(orderId).orElse(null);
            DeliveryPartner partner = partnerById.get(partnerId);

            if (order == null || partner == null) {
                continue;
            }

            order.setPartner(partner);
            order.setStatus(OrderStatus.PICK_UP);
            orderRepository.save(order);

            result.put(orderId, partnerId);
            log.info("Assigned order {} to partner {}", orderId, partnerId);
        }

        log.info("Total assignments applied: {}", result.size());
        return result;
    }

    /**
     * Convenience method called by controllers or schedulers.
     */
    @Transactional
    public Map<Long, Long> optimizeAssignmentsForReadyOrders() throws ExecutionException, InterruptedException {
        return runSolverAndApplyAssignments();
    }
}



