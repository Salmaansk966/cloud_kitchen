package com.cloudkitchen.service;

import com.cloudkitchen.dto.customer.CartDto;
import com.cloudkitchen.dto.customer.CartItemDto;
import com.cloudkitchen.dto.customer.OrderHistoryItemDto;
import com.cloudkitchen.dto.customer.OrderRequestDto;
import com.cloudkitchen.dto.customer.OrderResponseDto;
import com.cloudkitchen.dto.customer.ReviewRequestDto;
import com.cloudkitchen.dto.customer.ReviewResponseDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface CustomerService {

    CartDto getCart(Authentication auth);

    CartDto addToCart(Authentication auth, CartItemDto request);

    CartDto updateCartItem(Authentication auth, CartItemDto request);

    CartDto removeCartItem(Authentication auth, Long menuItemId);

    OrderResponseDto placeOrder(Authentication auth, OrderRequestDto request);

    List<OrderHistoryItemDto> getOrderHistory(Authentication auth);

    ReviewResponseDto addReview(Authentication auth, ReviewRequestDto request);
}





