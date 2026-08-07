package com.fashionpin.fashiondiscoveryservice.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HealthResponse {
    String status;
    String service;
}

