package com.fashionpin.outfitdetectionservice.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionResultResponse {
    private String id;
    private String jobId;
    private List<DetectedItemDto> detectedItems;
    private Instant createdAt;
}
