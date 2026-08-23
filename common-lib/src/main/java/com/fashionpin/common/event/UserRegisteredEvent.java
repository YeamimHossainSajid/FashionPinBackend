package com.fashionpin.common.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String userId;
    private String email;

    public static UserRegisteredEvent create(String userId, String email, String correlationId) {
        return UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("UserRegistered")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .userId(userId)
                .email(email)
                .build();
    }
}
