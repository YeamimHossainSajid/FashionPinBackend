package com.fashionpin.orderservice.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestInterceptor feignCorrelationIdInterceptor() {
        return template -> {
            // Placeholder: propagate correlation/auth headers across Feign calls.
        };
    }
}

