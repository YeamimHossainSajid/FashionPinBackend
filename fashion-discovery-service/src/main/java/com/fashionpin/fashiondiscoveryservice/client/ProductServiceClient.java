package com.fashionpin.fashiondiscoveryservice.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "product-service", path = "/api/v1")
public interface ProductServiceClient {

    @GetMapping("/health")
    Map<String, Object> health();
}

