package com.fashionpin.shoppingservice.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
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
