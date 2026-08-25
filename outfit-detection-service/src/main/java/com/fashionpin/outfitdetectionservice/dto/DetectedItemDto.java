package com.fashionpin.outfitdetectionservice.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectedItemDto {
    private String id;
    private String category;
    private Double confidence;
    private Map<String, Object> boundingBox;
    private List<String> productCandidateIds;
    private Map<String, Object> attributes;
}
