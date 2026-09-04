package com.fashionpin.common.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FashionPostEventPayload {
    private UUID postId;
    private UUID authorUserId;
    private String caption;
    private String style;
    private String occasion;
    private List<String> tags;
    private List<UUID> mediaIds;
    private Instant timestamp;
}
