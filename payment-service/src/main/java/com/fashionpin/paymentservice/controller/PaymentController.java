package com.fashionpin.paymentservice.controller;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.paymentservice.config.SSLCommerzProperties;
import com.fashionpin.paymentservice.dto.PaymentInitResponseDTO;
import com.fashionpin.paymentservice.dto.PaymentRequestDTO;
import com.fashionpin.paymentservice.service.SSLCommerzService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping({"/api/payment", "/api/v1/payment"})
@RequiredArgsConstructor
@Tag(name = "Payment Gateway", description = "SSLCommerz v4 payment processing and callback endpoints")
public class PaymentController {

    private final SSLCommerzService sslCommerzService;
    private final SSLCommerzProperties sslCommerzProperties;

    /**
     * Endpoint 1: Initialize Payment Session
     * Accepts frontend payment request data, connects to SSLCommerz, and returns GatewayPageURL.
     */
    @PostMapping("/init")
    @Operation(summary = "Initialize SSLCommerz Payment Session", description = "Initiates transaction and returns payment gateway redirect URL")
    public ResponseEntity<ApiResponse<PaymentInitResponseDTO>> initPayment(@Valid @RequestBody PaymentRequestDTO request) {
        log.info("Received payment initialization request for amount: {} {}, customer: {}",
                request.getAmount(), request.getCurrency(), request.getCustomerEmail());

        PaymentInitResponseDTO response = sslCommerzService.initiatePayment(request);
        return ResponseEntity.ok(ApiResponse.ok("Payment session initialized successfully", response));
    }

    /**
     * Endpoint 2: Success Callback from SSLCommerz
     * Consumes application/x-www-form-urlencoded callback from SSLCommerz, validates with validator API,
     * updates business/database records, and redirects customer to the frontend success page.
     */
    @PostMapping(value = "/success", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "SSLCommerz Payment Success Callback", description = "Validates transaction and redirects user to frontend")
    public ResponseEntity<Void> paymentSuccess(@RequestParam Map<String, String> formData) {
        String tranId = formData.get("tran_id");
        String valId = formData.get("val_id");
        String amount = formData.get("amount");
        String cardType = formData.get("card_type");
        String bankTranId = formData.get("bank_tran_id");

        log.info("Received SSLCommerz success callback for tranId: {}, valId: {}, amount: {}, cardType: {}",
                tranId, valId, amount, cardType);

        // 1. Verify transaction authenticity via SSLCommerz validation API
        boolean isValid = sslCommerzService.validateTransaction(valId, tranId, amount);

        String frontendBase = sslCommerzProperties.getFrontendUrl().replaceAll("/+$", "");

        if (isValid) {
            log.info("Payment validated successfully for tranId: {}", tranId);

            // =========================================================================
            // TODO: DB Update Logic for Successful Payment:
            // 1. Find Order / Payment record by tranId in repository:
            //    Payment payment = paymentRepository.findByTransactionId(tranId)
            //        .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + tranId));
            // 2. Update payment status:
            //    payment.setStatus(PaymentStatus.PAID);
            //    payment.setValidationId(valId);
            //    payment.setBankTransactionId(bankTranId);
            //    payment.setPaymentMethod(cardType);
            //    payment.setPaidAt(Instant.now());
            //    paymentRepository.save(payment);
            // 3. Publish Kafka event / update order-service status:
            //    orderPaymentEventPublisher.publishPaymentCompleted(payment);
            // =========================================================================

            String redirectUrl = String.format("%s/order-success?tran_id=%s&val_id=%s&status=PAID",
                    frontendBase, tranId != null ? tranId : "", valId != null ? valId : "");

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        } else {
            log.warn("Payment validation FAILED for tranId: {}, valId: {}", tranId, valId);

            // =========================================================================
            // TODO: DB Update Logic for Validation Failure:
            // 1. Mark transaction as VALIDATION_FAILED or UNDER_REVIEW
            // 2. Alert operations or mark order pending manual review
            // =========================================================================

            String redirectUrl = String.format("%s/order-failed?tran_id=%s&error=validation_failed",
                    frontendBase, tranId != null ? tranId : "");

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        }
    }

    /**
     * Endpoint 3: Failure Callback from SSLCommerz
     * Handles failed payment state, updates DB, and redirects user to frontend failed page.
     */
    @PostMapping(value = "/fail", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "SSLCommerz Payment Failure Callback", description = "Handles failed transaction and redirects user to frontend")
    public ResponseEntity<Void> paymentFail(@RequestParam Map<String, String> formData) {
        String tranId = formData.get("tran_id");
        String error = formData.get("error");
        log.warn("SSLCommerz payment failed for tranId: {}, error: {}", tranId, error);

        // =========================================================================
        // TODO: DB Update Logic for Failed Payment:
        // 1. Find transaction by tranId and update status to FAILED:
        //    paymentRepository.findByTransactionId(tranId).ifPresent(p -> {
        //        p.setStatus(PaymentStatus.FAILED);
        //        p.setFailureReason(error);
        //        paymentRepository.save(p);
        //    });
        // 2. Release temporary stock reservation if needed
        // =========================================================================

        String frontendBase = sslCommerzProperties.getFrontendUrl().replaceAll("/+$", "");
        String redirectUrl = String.format("%s/order-failed?tran_id=%s&status=FAILED&error=%s",
                frontendBase,
                tranId != null ? tranId : "",
                error != null ? error : "Transaction+declined");

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    /**
     * Endpoint 4: Cancellation Callback from SSLCommerz
     * Handles cancelled payment state, updates DB, and redirects user to frontend cancel page.
     */
    @PostMapping(value = "/cancel", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "SSLCommerz Payment Cancel Callback", description = "Handles cancelled transaction and redirects user to frontend")
    public ResponseEntity<Void> paymentCancel(@RequestParam Map<String, String> formData) {
        String tranId = formData.get("tran_id");
        log.info("SSLCommerz payment cancelled by user for tranId: {}", tranId);

        // =========================================================================
        // TODO: DB Update Logic for Cancelled Payment:
        // 1. Find transaction by tranId and update status to CANCELLED:
        //    paymentRepository.findByTransactionId(tranId).ifPresent(p -> {
        //        p.setStatus(PaymentStatus.CANCELLED);
        //        paymentRepository.save(p);
        //    });
        // 2. Restore cart or inventory state
        // =========================================================================

        String frontendBase = sslCommerzProperties.getFrontendUrl().replaceAll("/+$", "");
        String redirectUrl = String.format("%s/order-cancelled?tran_id=%s&status=CANCELLED",
                frontendBase,
                tranId != null ? tranId : "");

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    /**
     * IPN (Instant Payment Notification) Callback from SSLCommerz
     * Server-to-server webhook callback for asynchronous transaction status updates.
     */
    @PostMapping(value = "/ipn", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "SSLCommerz IPN Webhook", description = "Instant Payment Notification webhook from SSLCommerz")
    public ResponseEntity<String> paymentIpn(@RequestParam Map<String, String> formData) {
        String tranId = formData.get("tran_id");
        String valId = formData.get("val_id");
        String amount = formData.get("amount");
        log.info("Received SSLCommerz IPN webhook for tranId: {}, valId: {}", tranId, valId);

        boolean isValid = sslCommerzService.validateTransaction(valId, tranId, amount);
        if (isValid) {
            // =========================================================================
            // TODO: DB Update Logic for IPN Confirmation (idempotent update)
            // =========================================================================
            return ResponseEntity.ok("IPN_PROCESSED_SUCCESSFULLY");
        } else {
            return ResponseEntity.badRequest().body("IPN_VALIDATION_FAILED");
        }
    }
}
