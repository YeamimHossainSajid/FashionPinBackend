package com.fashionpin.productservice.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCollectionRequest {
    private String name;
    private String slug;
    private String tagline;
    private String description;
    private String heroMediaId;
    private String heroMediaUrl;
    private String accentColor;
    private Integer displayOrder;
    private Boolean isFeatured;
    private Boolean isActive;
    private Instant startsAt;
    private Instant endsAt;
}
