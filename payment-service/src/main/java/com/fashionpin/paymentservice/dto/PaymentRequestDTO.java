package com.fashionpin.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "1.00", message = "Minimum transaction amount is 1.00")
    @com.fasterxml.jackson.annotation.JsonAlias({"totalAmount", "total_amount", "amount"})
    private BigDecimal amount;

    private String currency;

    private String tranId;

    private String orderId;

    @NotBlank(message = "Customer name is required")
    @com.fasterxml.jackson.annotation.JsonAlias({"cusName", "cus_name", "customerName", "customer_name"})
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Please provide a valid customer email")
    @com.fasterxml.jackson.annotation.JsonAlias({"cusEmail", "cus_email", "customerEmail", "customer_email"})
    private String customerEmail;

    @NotBlank(message = "Customer phone number is required")
    @com.fasterxml.jackson.annotation.JsonAlias({"cusPhone", "cus_phone", "customerPhone", "customer_phone"})
    private String customerPhone;

    @com.fasterxml.jackson.annotation.JsonAlias({"cusAdd1", "cus_add1", "customerAddress", "customer_address", "address"})
    private String customerAddress;

    @com.fasterxml.jackson.annotation.JsonAlias({"cusCity", "cus_city", "customerCity", "customer_city", "city"})
    private String customerCity;

    @com.fasterxml.jackson.annotation.JsonAlias({"cusCountry", "cus_country", "customerCountry", "customer_country", "country"})
    private String customerCountry;

    private String productName;

    private String productCategory;

    private String productProfile;
}
