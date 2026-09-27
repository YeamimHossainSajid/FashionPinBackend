package com.fashionpin.orderservice.service;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.common.event.OrderCreatedEvent;
import com.fashionpin.common.event.OrderPaidEvent;
import com.fashionpin.common.exception.BusinessException;
import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.orderservice.client.CartServiceClient;
import com.fashionpin.orderservice.client.CartServiceClient.CartDto;
import com.fashionpin.orderservice.dto.CheckoutRequest;
import com.fashionpin.orderservice.dto.OrderItemDto;
import com.fashionpin.orderservice.dto.OrderResponse;
import com.fashionpin.orderservice.dto.ShippingAddressDto;
import com.fashionpin.orderservice.entity.Order;
import com.fashionpin.orderservice.entity.OrderItem;
import com.fashionpin.orderservice.entity.OrderShippingAddress;
import com.fashionpin.orderservice.entity.OrderStatus;
import com.fashionpin.orderservice.kafka.OrderEventProducer;
import com.fashionpin.orderservice.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartServiceClient cartServiceClient;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderResponse processCheckout(String userId, String guestToken, CheckoutRequest request) {
        log.info("Processing checkout for user: {}, guest: {}", userId, guestToken);

        // 1. Fetch Cart via Feign
        CartDto cart = null;
        try {
            ApiResponse<CartDto> cartResp = cartServiceClient.getCart(null, guestToken);
            if (cartResp != null && cartResp.getData() != null) {
                cart = cartResp.getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch cart from shopping-service: {}", e.getMessage());
        }

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException("EMPTY_CART_CHECKOUT", "Cannot checkout an empty shopping cart");
        }

        // 2. Compute order financials
        BigDecimal subtotal = cart.getSubtotal() != null ? cart.getSubtotal() : BigDecimal.ZERO;
        BigDecimal discount = cart.getDiscountAmount() != null ? cart.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal shipping = subtotal.compareTo(new BigDecimal("150.00")) >= 0 ? BigDecimal.ZERO : new BigDecimal("15.00");
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.08")).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal total = subtotal.subtract(discount).add(shipping).add(tax);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String orderNumber = "FP-" + datePrefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 3. Payment Stub Execution
        String transactionId = "tx_stub_" + UUID.randomUUID().toString().substring(0, 12);
        log.info("Payment stub executed successfully for orderNumber: {} with txId: {}", orderNumber, transactionId);

        // 4. Create Order Entity
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .email(request.getEmail())
                .status(OrderStatus.PAID)
                .currency(cart.getCurrency() != null ? cart.getCurrency() : "USD")
                .subtotal(subtotal)
                .discountAmount(discount)
                .shippingAmount(shipping)
                .taxAmount(tax)
                .totalAmount(total)
                .promoCode(cart.getPromoCode())
                .paymentMethod(request.getPaymentMethod())
                .paymentTransactionId(transactionId)
                .build();

        // 5. Attach Shipping Address
        ShippingAddressDto sDto = request.getShippingAddress();
        OrderShippingAddress address = OrderShippingAddress.builder()
                .order(order)
                .fullName(sDto.getFullName())
                .phoneNumber(sDto.getPhoneNumber())
                .addressLine1(sDto.getAddressLine1())
                .addressLine2(sDto.getAddressLine2())
                .city(sDto.getCity())
                .stateProvince(sDto.getStateProvince())
                .postalCode(sDto.getPostalCode())
                .countryCode(sDto.getCountryCode())
                .build();
        order.setShippingAddress(address);

        // 6. Attach Line Items
        for (var cItem : cart.getItems()) {
            OrderItem oItem = OrderItem.builder()
                    .order(order)
                    .productId(cItem.getProductId())
                    .variantId(cItem.getVariantId())
                    .sku(cItem.getSku())
                    .productName(cItem.getProductName())
                    .color(cItem.getColor())
                    .size(cItem.getSize())
                    .quantity(cItem.getQuantity())
                    .unitPrice(cItem.getUnitPrice())
                    .totalPrice(cItem.getTotalPrice())
                    .build();
            order.addItem(oItem);
        }

        Order saved = orderRepository.save(order);

        // 7. Clear Cart via Feign
        try {
            if (cart.getId() != null) {
                cartServiceClient.clearCartInternal(cart.getId());
            }
        } catch (Exception e) {
            log.warn("Failed to clear cart {} after checkout: {}", cart.getId(), e.getMessage());
        }

        // 8. Publish Events
        orderEventProducer.publishOrderCreated(OrderCreatedEvent.create(
                saved.getId(), saved.getOrderNumber(), saved.getUserId(), saved.getEmail(), saved.getTotalAmount(), saved.getCurrency(), null));

        orderEventProducer.publishOrderPaid(OrderPaidEvent.create(
                saved.getId(), saved.getOrderNumber(), saved.getPaymentTransactionId(), saved.getTotalAmount(), null));

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(String userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(String orderIdOrNumber, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderIdOrNumber)
                .or(() -> orderRepository.findByOrderNumber(orderIdOrNumber))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderIdOrNumber));
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        log.info("Order {} status updated to {}", order.getOrderNumber(), newStatus);
        return mapToResponse(updated);
    }

    public OrderResponse mapToResponse(Order order) {
        ShippingAddressDto addressDto = null;
        if (order.getShippingAddress() != null) {
            var addr = order.getShippingAddress();
            addressDto = ShippingAddressDto.builder()
                    .fullName(addr.getFullName())
                    .phoneNumber(addr.getPhoneNumber())
                    .addressLine1(addr.getAddressLine1())
                    .addressLine2(addr.getAddressLine2())
                    .city(addr.getCity())
                    .stateProvince(addr.getStateProvince())
                    .postalCode(addr.getPostalCode())
                    .countryCode(addr.getCountryCode())
                    .build();
        }

        List<OrderItemDto> itemDtos = order.getItems() != null
                ? order.getItems().stream()
                        .map(i -> OrderItemDto.builder()
                                .id(i.getId())
                                .productId(i.getProductId())
                                .variantId(i.getVariantId())
                                .sku(i.getSku())
                                .productName(i.getProductName())
                                .color(i.getColor())
                                .size(i.getSize())
                                .quantity(i.getQuantity())
                                .unitPrice(i.getUnitPrice())
                                .totalPrice(i.getTotalPrice())
                                .build())
                        .toList()
                : List.of();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .email(order.getEmail())
                .status(order.getStatus())
                .currency(order.getCurrency())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .shippingAmount(order.getShippingAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .promoCode(order.getPromoCode())
                .paymentMethod(order.getPaymentMethod())
                .paymentTransactionId(order.getPaymentTransactionId())
                .shippingAddress(addressDto)
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
