package com.fashionpin.orderservice.client;

import com.fashionpin.common.dto.ApiResponse;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "shopping-service")
public interface CartServiceClient {

    @GetMapping("/api/cart")
    ApiResponse<CartDto> getCart(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @RequestHeader(name = "X-Guest-Session-Token", required = false) String guestToken);

    @DeleteMapping("/api/cart/internal/{cartId}")
    ApiResponse<Void> clearCartInternal(@PathVariable("cartId") String cartId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CartDto {
        private String id;
        private String userId;
        private String guestSessionToken;
        private List<CartItemDto> items;
        private Integer totalQuantity;
        private BigDecimal subtotal;
        private String promoCode;
        private BigDecimal discountAmount;
        private BigDecimal total;
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CartItemDto {
        private String id;
        private String productId;
        private String variantId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String currency;
        private String productName;
        private String productImage;
        private String color;
        private String size;
        private String sku;
    }
}
