package com.fashionpin.orderservice.controller;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.orderservice.dto.CheckoutRequest;
import com.fashionpin.orderservice.dto.OrderResponse;
import com.fashionpin.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Checkout and Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping({"/api/orders/checkout", "/api/v1/orders/checkout"})
    @Operation(summary = "Process checkout from shopping cart with payment stub")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            Principal principal,
            @RequestHeader(name = "X-Guest-Session-Token", required = false) String guestToken,
            @Valid @RequestBody CheckoutRequest request) {
        String userId = principal != null ? principal.getName() : null;
        OrderResponse response = orderService.processCheckout(userId, guestToken, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order placed successfully", response));
    }

    @GetMapping({"/api/orders/{orderNumber}", "/api/v1/orders/{orderNumber}"})
    @Operation(summary = "Get order confirmation and tracking details by order number")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(@PathVariable("orderNumber") String orderNumber) {
        OrderResponse response = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping({"/api/orders/my-orders", "/api/v1/orders/my-orders"})
    @Operation(summary = "Get order history for authenticated user")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            Principal principal,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> response = orderService.getMyOrders(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
