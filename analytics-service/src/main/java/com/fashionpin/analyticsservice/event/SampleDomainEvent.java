package com.fashionpin.analyticsservice.event;

import com.fashionpin.common.event.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SampleDomainEvent extends DomainEvent {

    public static SampleDomainEvent foundationReady(String correlationId) {
        return SampleDomainEvent.builder()
                .eventType("FOUNDATION_READY")
                .source("analytics-service")
                .correlationId(correlationId)
                .aggregateId("analytics-service")
                .aggregateType("Service")
                .payload(java.util.Map.of("status", "ready"))
                .build();
    }
}

