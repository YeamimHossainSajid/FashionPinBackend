package com.fashionpin.fashiondiscoveryservice.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public Map<String, String> currentStatus() {
        return Map.of(
                "status", "UP",
                "service", "fashion-discovery-service");
    }
}

