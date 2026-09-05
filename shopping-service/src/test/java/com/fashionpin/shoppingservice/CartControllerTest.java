package com.fashionpin.shoppingservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fashionpin.shoppingservice.dto.AddToCartRequest;
import com.fashionpin.shoppingservice.dto.CartItemDto;
import com.fashionpin.shoppingservice.dto.CartResponse;
import com.fashionpin.shoppingservice.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @Test
    void getCart_Guest_Success() throws Exception {
        CartResponse cartResponse = CartResponse.builder()
                .id("cart-123")
                .guestSessionToken("guest-token-1")
                .totalQuantity(2)
                .subtotal(new BigDecimal("198.00"))
                .total(new BigDecimal("198.00"))
                .currency("USD")
                .items(List.of(
                        CartItemDto.builder()
                                .id("item-1")
                                .productId("prod-1")
                                .variantId("var-1")
                                .productName("Silk Dress")
                                .quantity(2)
                                .unitPrice(new BigDecimal("99.00"))
                                .totalPrice(new BigDecimal("198.00"))
                                .currency("USD")
                                .build()
                ))
                .build();

        given(cartService.getCart(eq(null), eq("guest-token-1"))).willReturn(cartResponse);

        mockMvc.perform(get("/api/cart")
                        .header("X-Guest-Session-Token", "guest-token-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("cart-123"))
                .andExpect(jsonPath("$.data.totalQuantity").value(2))
                .andExpect(jsonPath("$.data.items[0].productName").value("Silk Dress"));
    }

    @Test
    void addToCart_Success() throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId("prod-1")
                .variantId("var-1")
                .quantity(1)
                .build();

        CartResponse cartResponse = CartResponse.builder()
                .id("cart-123")
                .guestSessionToken("guest-token-1")
                .totalQuantity(1)
                .subtotal(new BigDecimal("99.00"))
                .total(new BigDecimal("99.00"))
                .currency("USD")
                .build();

        given(cartService.addToCart(eq(null), eq("guest-token-1"), any(AddToCartRequest.class))).willReturn(cartResponse);

        mockMvc.perform(post("/api/cart/items")
                        .header("X-Guest-Session-Token", "guest-token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("cart-123"))
                .andExpect(jsonPath("$.data.totalQuantity").value(1));
    }

    @Test
    void removeCartItem_Success() throws Exception {
        CartResponse cartResponse = CartResponse.builder()
                .id("cart-123")
                .totalQuantity(0)
                .subtotal(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .items(List.of())
                .build();

        given(cartService.removeCartItem(eq(null), eq("guest-token-1"), eq("item-1"))).willReturn(cartResponse);

        mockMvc.perform(delete("/api/cart/items/item-1")
                        .header("X-Guest-Session-Token", "guest-token-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalQuantity").value(0));
    }
}
