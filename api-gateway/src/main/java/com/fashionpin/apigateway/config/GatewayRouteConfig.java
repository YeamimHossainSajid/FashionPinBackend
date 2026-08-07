package com.fashionpin.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        // Routes are primarily defined in application.yml via Eureka lb:// URIs.
        // This bean is a placeholder for programmatic route extensions.
        return builder.routes().build();
    }
}

