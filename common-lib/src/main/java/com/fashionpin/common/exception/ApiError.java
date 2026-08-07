package com.fashionpin.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiError {
    Instant timestamp;
    int status;
    String error;
    String message;
    String path;
    String correlationId;
    List<String> details;
}

