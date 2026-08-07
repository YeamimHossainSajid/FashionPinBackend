package com.fashionpin.common.exception;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {
    Instant timestamp;
    String code;
    String message;
    List<String> details;
    String path;
    String correlationId;
}

