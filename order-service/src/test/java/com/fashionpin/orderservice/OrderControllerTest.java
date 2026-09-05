package com.fashionpin.orderservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fashionpin.orderservice.dto.CheckoutRequest;
import com.fashionpin.orderservice.dto.OrderItemDto;
import com.fashionpin.orderservice.dto.OrderResponse;
import com.fashionpin.orderservice.dto.ShippingAddressDto;
import com.fashionpin.orderservice.entity.OrderStatus;
import com.fashionpin.orderservice.service.OrderService;
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
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void checkout_Success() throws Exception {
        ShippingAddressDto address = ShippingAddressDto.builder()
                .fullName("Jane Doe")
                .phoneNumber("+15551234567")
                .addressLine1("123 Fashion Blvd")
                .city("New York")
                .stateProvince("NY")
                .postalCode("10001")
                .countryCode("US")
                .build();

        CheckoutRequest request = CheckoutRequest.builder()
                .email("jane@example.com")
                .shippingAddress(address)
                .paymentMethod("CREDIT_CARD_STUB")
                .paymentToken("tok_visa_success")
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .id("order-1")
                .orderNumber("FP-20260906-ABCD1234")
                .email("jane@example.com")
                .status(OrderStatus.PAID)
                .subtotal(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("162.00"))
                .currency("USD")
                .paymentMethod("CREDIT_CARD_STUB")
                .shippingAddress(address)
                .items(List.of(
                        OrderItemDto.builder()
                                .productId("prod-1")
                                .productName("Silk Dress")
                                .quantity(1)
                                .unitPrice(new BigDecimal("150.00"))
                                .totalPrice(new BigDecimal("150.00"))
                                .build()
                ))
                .build();

        given(orderService.processCheckout(eq(null), eq("guest-token-1"), any(CheckoutRequest.class))).willReturn(orderResponse);

        mockMvc.perform(post("/api/orders/checkout")
                        .header("X-Guest-Session-Token", "guest-token-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderNumber").value("FP-20260906-ABCD1234"))
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    @Test
    void getOrderByNumber_Success() throws Exception {
        OrderResponse orderResponse = OrderResponse.builder()
                .id("order-1")
                .orderNumber("FP-20260906-ABCD1234")
                .email("jane@example.com")
                .status(OrderStatus.PAID)
                .totalAmount(new BigDecimal("162.00"))
                .build();

        given(orderService.getOrderByNumber("FP-20260906-ABCD1234")).willReturn(orderResponse);

        mockMvc.perform(get("/api/orders/FP-20260906-ABCD1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNumber").value("FP-20260906-ABCD1234"));
    }
}
