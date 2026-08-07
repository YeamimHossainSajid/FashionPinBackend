package com.fashionpin.brandintegrationservice.util;

import com.fashionpin.common.util.CorrelationIdConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CorrelationIdInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
        String correlationId = request.getHeader(CorrelationIdConstants.CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(CorrelationIdConstants.MDC_CORRELATION_ID, correlationId);
        response.setHeader(CorrelationIdConstants.CORRELATION_ID_HEADER, correlationId);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        MDC.remove(CorrelationIdConstants.MDC_CORRELATION_ID);
    }
}

