package com.fashionpin.common.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualMatchDto {
    private UUID productId;
    private float similarityScore;
    private String category;
    private String matchedRegionBoundingBox;
}
