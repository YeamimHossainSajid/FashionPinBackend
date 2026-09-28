package com.fashionpin.paymentservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fashionpin.paymentservice.config.SSLCommerzProperties;
import com.fashionpin.paymentservice.dto.PaymentInitResponseDTO;
import com.fashionpin.paymentservice.dto.PaymentRequestDTO;
import com.fashionpin.paymentservice.dto.SSLCommerzInitResponseDTO;
import com.fashionpin.paymentservice.dto.SSLCommerzValidatorResponseDTO;
import com.fashionpin.paymentservice.service.SSLCommerzService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SSLCommerzServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private SSLCommerzProperties properties;
    private SSLCommerzService sslCommerzService;

    @BeforeEach
    void setUp() {
        properties = new SSLCommerzProperties();
        properties.setStoreId("test_store_id");
        properties.setStorePassword("test_store_passwd");
        properties.setInitUrl("https://sandbox.sslcommerz.com/gwprocess/v4/api.php");
        properties.setValidationUrl("https://sandbox.sslcommerz.com/validator/api/validationserverAPI.php");
        properties.setBackendUrl("http://localhost:8080");
        properties.setFrontendUrl("http://localhost:3000");

        sslCommerzService = new SSLCommerzService(properties, restTemplate);
    }

    @Test
    void testInitiatePayment_Success() {
        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .amount(new BigDecimal("1500.00"))
                .currency("BDT")
                .customerName("Rahim Ahmed")
                .customerEmail("rahim@example.com")
                .customerPhone("+8801700000000")
                .customerAddress("Gulshan 2, Road 45")
                .customerCity("Dhaka")
                .customerCountry("Bangladesh")
                .productName("Haute Silk Gown")
                .productCategory("Couture")
                .build();

        SSLCommerzInitResponseDTO mockResponse = SSLCommerzInitResponseDTO.builder()
                .status("SUCCESS")
                .sessionkey("TEST_SESSION_KEY_12345")
                .gatewayPageURL("https://sandbox.sslcommerz.com/EasyCheckout/testcde12345")
                .build();

        when(restTemplate.postForEntity(eq(properties.getInitUrl()), any(HttpEntity.class), eq(SSLCommerzInitResponseDTO.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        PaymentInitResponseDTO result = sslCommerzService.initiatePayment(request);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertNotNull(result.getTranId());
        assertEquals("https://sandbox.sslcommerz.com/EasyCheckout/testcde12345", result.getGatewayPageURL());
    }

    @Test
    void testValidateTransaction_Success() {
        String valId = "VAL_123456789";
        String tranId = "TRX-TEST98765";
        String amount = "1500.00";

        SSLCommerzValidatorResponseDTO mockResponse = SSLCommerzValidatorResponseDTO.builder()
                .status("VALID")
                .tran_id(tranId)
                .val_id(valId)
                .amount("1500.00")
                .currency("BDT")
                .card_type("BKASH-BKash")
                .bank_tran_id("BANK_TRX_999")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(SSLCommerzValidatorResponseDTO.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        boolean isValid = sslCommerzService.validateTransaction(valId, tranId, amount);

        assertTrue(isValid);
    }

    @Test
    void testValidateTransaction_AmountMismatch() {
        String valId = "VAL_123456789";
        String tranId = "TRX-TEST98765";
        String amount = "1500.00";

        SSLCommerzValidatorResponseDTO mockResponse = SSLCommerzValidatorResponseDTO.builder()
                .status("VALID")
                .tran_id(tranId)
                .val_id(valId)
                .amount("999.00") // Mismatched amount
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(SSLCommerzValidatorResponseDTO.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        boolean isValid = sslCommerzService.validateTransaction(valId, tranId, amount);

        assertFalse(isValid);
    }

    @Test
    void testValidateTransaction_StatusFailed() {
        String valId = "VAL_123456789";
        String tranId = "TRX-TEST98765";
        String amount = "1500.00";

        SSLCommerzValidatorResponseDTO mockResponse = SSLCommerzValidatorResponseDTO.builder()
                .status("FAILED")
                .tran_id(tranId)
                .val_id(valId)
                .amount("1500.00")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(SSLCommerzValidatorResponseDTO.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        boolean isValid = sslCommerzService.validateTransaction(valId, tranId, amount);

        assertFalse(isValid);
    }
}
