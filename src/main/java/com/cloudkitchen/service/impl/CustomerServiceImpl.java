package com.cloudkitchen.service.impl;

import com.cloudkitchen.dto.customer.CartDto;
import com.cloudkitchen.dto.customer.CartItemDto;
import com.cloudkitchen.dto.customer.OrderHistoryItemDto;
import com.cloudkitchen.dto.customer.OrderRequestDto;
import com.cloudkitchen.dto.customer.OrderResponseDto;
import com.cloudkitchen.dto.customer.ReviewRequestDto;
import com.cloudkitchen.dto.customer.ReviewResponseDto;
import com.cloudkitchen.entity.Cart;
import com.cloudkitchen.entity.CartItem;
import com.cloudkitchen.entity.MenuItem;
import com.cloudkitchen.entity.Order;
import com.cloudkitchen.entity.OrderItem;
import com.cloudkitchen.entity.Payment;
import com.cloudkitchen.entity.Review;
import com.cloudkitchen.entity.User;
import com.cloudkitchen.entity.enums.OrderStatus;
import com.cloudkitchen.entity.enums.PaymentStatus;
import com.cloudkitchen.exception.BadRequestException;
import com.cloudkitchen.exception.ResourceNotFoundException;
import com.cloudkitchen.repository.CartItemRepository;
import com.cloudkitchen.repository.CartRepository;
import com.cloudkitchen.repository.MenuItemRepository;
import com.cloudkitchen.repository.OrderRepository;
import com.cloudkitchen.repository.OrderItemRepository;
import com.cloudkitchen.repository.PaymentRepository;
import com.cloudkitchen.repository.ReviewRepository;
import com.cloudkitchen.repository.UserRepository;
import com.cloudkitchen.repository.DeliveryPartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements com.cloudkitchen.service.CustomerService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final TrackingServiceImpl trackingService;

    private User getCurrentUser(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Cart getOrCreateCart(User customer) {
        return cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    Cart cart = Cart.builder()
                            .customer(customer)
                            .build();
                    return cartRepository.save(cart);
                });
    }

    @Override
    public CartDto getCart(Authentication auth) {
        User user = getCurrentUser(auth);
        Cart cart = getOrCreateCart(user);
        return toCartDto(cart);
    }

    @Override
    @Transactional
    public CartDto addToCart(Authentication auth, CartItemDto request) {
        if (request.getMenuItemId() == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BadRequestException("Invalid cart item");
        }

        User user = getCurrentUser(auth);
        Cart cart = getOrCreateCart(user);
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        CartItem existing = cart.getItems() == null ? null :
                cart.getItems().stream()
                        .filter(i -> i.getMenuItem().getId().equals(menuItem.getId()))
                        .findFirst()
                        .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            cartItemRepository.save(existing);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
        }

        Cart refreshed = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        return toCartDto(refreshed);
    }

    @Override
    @Transactional
    public CartDto updateCartItem(Authentication auth, CartItemDto request) {
        if (request.getMenuItemId() == null || request.getQuantity() == null || request.getQuantity() < 0) {
            throw new BadRequestException("Invalid cart item");
        }

        User user = getCurrentUser(auth);
        Cart cart = getOrCreateCart(user);

        if (cart.getItems() == null) {
            throw new BadRequestException("Cart is empty");
        }

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getMenuItem().getId().equals(request.getMenuItemId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not in cart"));

        if (request.getQuantity() == 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }

        Cart refreshed = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        return toCartDto(refreshed);
    }

    @Override
    @Transactional
    public CartDto removeCartItem(Authentication auth, Long menuItemId) {
        User user = getCurrentUser(auth);
        Cart cart = getOrCreateCart(user);

        if (cart.getItems() == null) {
            return toCartDto(cart);
        }

        cart.getItems().stream()
                .filter(i -> i.getMenuItem().getId().equals(menuItemId))
                .findFirst()
                .ifPresent(cartItemRepository::delete);

        Cart refreshed = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        return toCartDto(refreshed);
    }

    @Override
    @Transactional
    public OrderResponseDto placeOrder(Authentication auth, OrderRequestDto request) {
        User user = getCurrentUser(auth);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        double total = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderRequestDto.Item itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
            double itemTotal = menuItem.getPrice() * itemReq.getQuantity();
            total += itemTotal;
            OrderItem oi = OrderItem.builder()
                    .menuItem(menuItem)
                    .quantity(itemReq.getQuantity())
                    .price(menuItem.getPrice())
                    .build();
            orderItems.add(oi);
        }

        Order order = Order.builder()
                .customer(user)
                .status(OrderStatus.PLACED)
                .paymentMethod(request.getPaymentMethod())
                .totalAmount(total)
                .createdAt(OffsetDateTime.now())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryLat(request.getDeliveryLat())
                .deliveryLng(request.getDeliveryLng())
                .build();

        // assign nearest partner (if any)
        if (request.getDeliveryLat() != null && request.getDeliveryLng() != null) {
            Long partnerId = trackingService.findNearestPartner(request.getDeliveryLat(), request.getDeliveryLng());
            if (partnerId != null) {
                deliveryPartnerRepository.findById(partnerId).ifPresent(order::setPartner);
            }
        }

        Order savedOrder = orderRepository.save(order);
        for (OrderItem oi : orderItems) {
            oi.setOrder(savedOrder);
        }
        orderItems = orderItems.stream()
                .map(orderItemRepository::save)
                .collect(Collectors.toList());
        savedOrder.setItems(orderItems);

        Payment payment = Payment.builder()
                .order(savedOrder)
                .method(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .amount(total)
                .createdAt(OffsetDateTime.now())
                .build();
        paymentRepository.save(payment);
        savedOrder.setPayment(payment);

        // clear cart
        Cart cart = getOrCreateCart(user);
        if (cart.getItems() != null) {
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
        }

        return toOrderResponseDto(savedOrder);
    }

    @Override
    public List<OrderHistoryItemDto> getOrderHistory(Authentication auth) {
        User user = getCurrentUser(auth);
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(order -> OrderHistoryItemDto.builder()
                        .orderId(order.getId())
                        .totalAmount(order.getTotalAmount())
                        .status(order.getStatus().name())
                        .createdAt(order.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ReviewResponseDto addReview(Authentication auth, ReviewRequestDto request) {
        User user = getCurrentUser(auth);
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Review review = Review.builder()
                .order(order)
                .customer(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(OffsetDateTime.now())
                .build();

        if (request.getMenuItemId() != null) {
            MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
            review.setMenuItem(menuItem);
        }

        Review saved = reviewRepository.save(review);

        return ReviewResponseDto.builder()
                .reviewId(saved.getId())
                .rating(saved.getRating())
                .comment(saved.getComment())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private CartDto toCartDto(Cart cart) {
        if (cart.getItems() == null) {
            return CartDto.builder()
                    .cartId(cart.getId())
                    .items(List.of())
                    .total(0.0)
                    .build();
        }
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(i -> CartItemDto.builder()
                        .menuItemId(i.getMenuItem().getId())
                        .name(i.getMenuItem().getName())
                        .price(i.getMenuItem().getPrice())
                        .quantity(i.getQuantity())
                        .build())
                .collect(Collectors.toList());
        double total = itemDtos.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        return CartDto.builder()
                .cartId(cart.getId())
                .items(itemDtos)
                .total(total)
                .build();
    }

    private OrderResponseDto toOrderResponseDto(Order order) {
        List<OrderResponseDto.OrderItemDto> items = order.getItems() == null ? List.of() :
                order.getItems().stream()
                        .map(oi -> OrderResponseDto.OrderItemDto.builder()
                                .menuItemId(oi.getMenuItem().getId())
                                .itemName(oi.getMenuItem().getName())
                                .quantity(oi.getQuantity())
                                .price(oi.getPrice())
                                .build())
                        .collect(Collectors.toList());
        return OrderResponseDto.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}


