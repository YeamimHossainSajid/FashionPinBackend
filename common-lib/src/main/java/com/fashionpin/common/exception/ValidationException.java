package com.fashionpin.common.exception;

import java.util.List;
import lombok.Getter;

@Getter
public class ValidationException extends BusinessException {
    private final List<String> details;

    public ValidationException(String message, List<String> details) {
        super("VALIDATION_ERROR", message);
        this.details = details;
    }
}

