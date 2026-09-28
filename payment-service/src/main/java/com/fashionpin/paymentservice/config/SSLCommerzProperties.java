package com.fashionpin.paymentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sslcommerz")
public class SSLCommerzProperties {

    /**
     * SSLCommerz Merchant Store ID
     */
    private String storeId = "";

    /**
     * SSLCommerz Merchant Store Password
     */
    private String storePassword = "";

    /**
     * SSLCommerz V4 Session Initialization URL (Sandbox / Production)
     */
    private String initUrl = "https://sandbox.sslcommerz.com/gwprocess/v4/api.php";

    /**
     * SSLCommerz Order Validation API URL
     */
    private String validationUrl = "https://sandbox.sslcommerz.com/validator/api/validationserverAPI.php";

    /**
     * Base URL of the backend application for constructing callback endpoints (success, fail, cancel)
     */
    private String backendUrl = "http://localhost:8080";

    /**
     * Frontend application base URL for final customer redirection
     */
    private String frontendUrl = "http://localhost:3000";

    public void setStorePasswd(String storePasswd) {
        this.storePassword = storePasswd;
    }

    public void setBackendBaseUrl(String backendBaseUrl) {
        this.backendUrl = backendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendUrl = frontendBaseUrl;
    }
}
