package com.fashionpin.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DomainEvent extends BaseEvent {
    private String aggregateId;
    private String aggregateType;
    private Object payload;

    public DomainEvent(
            String eventType,
            String source,
            String correlationId,
            String aggregateId,
            String aggregateType,
            Object payload) {
        super(eventType, source, correlationId);
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.payload = payload;
    }
}

