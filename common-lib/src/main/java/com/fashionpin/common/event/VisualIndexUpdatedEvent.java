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
public class VisualIndexUpdatedEvent {
    private UUID productId;
    private String category;
    private int embeddingDimension;
    private Instant updatedAt;
}
