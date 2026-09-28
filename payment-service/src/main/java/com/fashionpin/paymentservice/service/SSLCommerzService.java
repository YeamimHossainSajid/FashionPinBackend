package com.fashionpin.paymentservice.service;

import com.fashionpin.paymentservice.config.SSLCommerzProperties;
import com.fashionpin.paymentservice.dto.PaymentInitResponseDTO;
import com.fashionpin.paymentservice.dto.PaymentRequestDTO;
import com.fashionpin.paymentservice.dto.SSLCommerzInitResponseDTO;
import com.fashionpin.paymentservice.dto.SSLCommerzValidatorResponseDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class SSLCommerzService {

    private final SSLCommerzProperties sslCommerzProperties;
    private final RestTemplate restTemplate;

    /**
     * Initializes a payment session with SSLCommerz v4 Gateway API.
     *
     * @param request Customer and payment details from frontend
     * @return PaymentInitResponseDTO containing GatewayPageURL and transaction ID
     */
    public PaymentInitResponseDTO initiatePayment(PaymentRequestDTO request) {
        log.info("Initiating SSLCommerz payment for customer: {}, amount: {}",
                request.getCustomerEmail(), request.getAmount());

        // 1. Generate unique transaction ID if not already supplied
        String tranId = (request.getTranId() != null && !request.getTranId().trim().isEmpty())
                ? request.getTranId().trim()
                : "TRX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        // 2. Set up headers for application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 3. Construct mandatory and optional parameters
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        // Store Credentials & Core Transaction Info
        params.add("store_id", sslCommerzProperties.getStoreId());
        params.add("store_passwd", sslCommerzProperties.getStorePassword());
        params.add("total_amount", String.format(Locale.US, "%.2f", request.getAmount()));
        params.add("currency", (request.getCurrency() != null && !request.getCurrency().trim().isEmpty())
                ? request.getCurrency().trim() : "BDT");
        params.add("tran_id", tranId);

        // Callback URLs dynamically constructed from configured backend URL
        String backendBase = sslCommerzProperties.getBackendUrl().replaceAll("/+$", "");
        params.add("success_url", backendBase + "/api/payment/success");
        params.add("fail_url", backendBase + "/api/payment/fail");
        params.add("cancel_url", backendBase + "/api/payment/cancel");
        params.add("ipn_url", backendBase + "/api/payment/ipn");

        // Customer Information
        params.add("cus_name", request.getCustomerName());
        params.add("cus_email", request.getCustomerEmail());
        params.add("cus_phone", request.getCustomerPhone());
        params.add("cus_add1", (request.getCustomerAddress() != null && !request.getCustomerAddress().trim().isEmpty())
                ? request.getCustomerAddress() : "Dhaka, Bangladesh");
        params.add("cus_city", (request.getCustomerCity() != null && !request.getCustomerCity().trim().isEmpty())
                ? request.getCustomerCity() : "Dhaka");
        params.add("cus_country", (request.getCustomerCountry() != null && !request.getCustomerCountry().trim().isEmpty())
                ? request.getCustomerCountry() : "Bangladesh");

        // Product Information
        params.add("shipping_method", "NO");
        params.add("num_of_item", "1");
        params.add("product_name", (request.getProductName() != null && !request.getProductName().trim().isEmpty())
                ? request.getProductName() : "FashionPin Atelier Couture");
        params.add("product_category", (request.getProductCategory() != null && !request.getProductCategory().trim().isEmpty())
                ? request.getProductCategory() : "Apparel");
        params.add("product_profile", (request.getProductProfile() != null && !request.getProductProfile().trim().isEmpty())
                ? request.getProductProfile() : "general");

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

        try {
            log.debug("Sending POST to SSLCommerz initialization endpoint: {}", sslCommerzProperties.getInitUrl());
            ResponseEntity<SSLCommerzInitResponseDTO> response = restTemplate.postForEntity(
                    sslCommerzProperties.getInitUrl(),
                    httpEntity,
                    SSLCommerzInitResponseDTO.class
            );

            SSLCommerzInitResponseDTO body = response.getBody();
            if (body != null && "SUCCESS".equalsIgnoreCase(body.getStatus()) && body.getGatewayPageURL() != null) {
                log.info("Payment session created successfully. TranId: {}, GatewayPageURL: {}",
                        tranId, body.getGatewayPageURL());

                return PaymentInitResponseDTO.builder()
                        .status("SUCCESS")
                        .tranId(tranId)
                        .gatewayPageURL(body.getGatewayPageURL())
                        .message("Payment session initialized successfully")
                        .build();
            } else {
                String errorReason = (body != null && body.getFailedreason() != null)
                        ? body.getFailedreason()
                        : "Unknown initialization error from SSLCommerz";
                log.error("SSLCommerz initialization failed. Status: {}, Reason: {}",
                        body != null ? body.getStatus() : "null", errorReason);

                throw new RuntimeException("SSLCommerz initialization failed: " + errorReason);
            }
        } catch (RestClientException ex) {
            log.error("Network or HTTP communication error with SSLCommerz gateway: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to communicate with SSLCommerz payment gateway: " + ex.getMessage(), ex);
        }
    }

    /**
     * Validates an incoming transaction callback with SSLCommerz validation API.
     *
     * @param valId Validation ID supplied by SSLCommerz in callback
     * @param tranId Transaction ID originally generated and stored
     * @param amount Transaction amount recorded in system
     * @return true if status is VALID/VALIDATED and both tranId and amount match strictly
     */
    public boolean validateTransaction(String valId, String tranId, String amount) {
        log.info("Validating transaction. valId: {}, tranId: {}, amount: {}", valId, tranId, amount);

        if (valId == null || valId.trim().isEmpty() || tranId == null || tranId.trim().isEmpty()) {
            log.warn("Validation failed: val_id or tran_id is missing");
            return false;
        }

        String validationUrl = UriComponentsBuilder.fromHttpUrl(sslCommerzProperties.getValidationUrl())
                .queryParam("val_id", valId.trim())
                .queryParam("store_id", sslCommerzProperties.getStoreId())
                .queryParam("store_passwd", sslCommerzProperties.getStorePassword())
                .queryParam("v", "1")
                .queryParam("format", "json")
                .toUriString();

        try {
            log.debug("Calling SSLCommerz validation endpoint: {}", validationUrl);
            ResponseEntity<SSLCommerzValidatorResponseDTO> response = restTemplate.exchange(
                    validationUrl,
                    HttpMethod.GET,
                    null,
                    SSLCommerzValidatorResponseDTO.class
            );

            SSLCommerzValidatorResponseDTO body = response.getBody();
            if (body == null) {
                log.error("Validation response body from SSLCommerz is null");
                return false;
            }

            log.info("SSLCommerz validation response status: {}, tran_id: {}, amount: {}",
                    body.getStatus(), body.getTran_id(), body.getAmount());

            // 1. Check status is VALID or VALIDATED
            boolean isStatusValid = "VALID".equalsIgnoreCase(body.getStatus())
                    || "VALIDATED".equalsIgnoreCase(body.getStatus());

            if (!isStatusValid) {
                log.warn("Transaction validation status invalid: {}", body.getStatus());
                return false;
            }

            // 2. Strict matching of transaction ID
            boolean isTranIdMatch = tranId.trim().equalsIgnoreCase(
                    body.getTran_id() != null ? body.getTran_id().trim() : ""
            );
            if (!isTranIdMatch) {
                log.error("Transaction ID mismatch! Expected: {}, Gateway returned: {}",
                        tranId, body.getTran_id());
                return false;
            }

            // 3. Strict matching of amount
            boolean isAmountMatch = isAmountsEqual(amount, body.getAmount());
            if (!isAmountMatch) {
                log.error("Transaction amount mismatch! Expected: {}, Gateway returned: {}",
                        amount, body.getAmount());
                return false;
            }

            log.info("Transaction successfully validated for tranId: {}", tranId);
            return true;

        } catch (RestClientException ex) {
            log.error("Error during SSLCommerz transaction validation call: {}", ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Safely compares two numeric amount strings taking decimals into account.
     */
    private boolean isAmountsEqual(String expectedAmount, String returnedAmount) {
        if (expectedAmount == null || returnedAmount == null) {
            return false;
        }
        try {
            BigDecimal expected = new BigDecimal(expectedAmount.trim()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal returned = new BigDecimal(returnedAmount.trim()).setScale(2, RoundingMode.HALF_UP);
            return expected.compareTo(returned) == 0;
        } catch (NumberFormatException e) {
            log.warn("Failed to parse amounts for comparison: expected={}, returned={}",
                    expectedAmount, returnedAmount);
            return expectedAmount.trim().equalsIgnoreCase(returnedAmount.trim());
        }
    }
}
