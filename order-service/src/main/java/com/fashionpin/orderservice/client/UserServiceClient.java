package com.fashionpin.orderservice.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-service", path = "/api/v1")
public interface UserServiceClient {

    @GetMapping("/health")
    Map<String, Object> health();
}

