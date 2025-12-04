package com.cloudkitchen.entity;

import com.cloudkitchen.entity.enums.OrderStatus;
import com.cloudkitchen.entity.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private DeliveryPartner partner;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private Double totalAmount;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime acceptedAt;
    private OffsetDateTime preparedAt;
    private OffsetDateTime readyAt;
    private OffsetDateTime pickedUpAt;
    private OffsetDateTime deliveredAt;

    private String deliveryAddress;
    private Double deliveryLat;
    private Double deliveryLng;
}





