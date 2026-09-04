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
public class VisualQueryRequestedEvent {
    private UUID jobId;
    private UUID userId;
    private String inputMediaId;
    private String queryImageUrl;
    private Instant requestedAt;
}
