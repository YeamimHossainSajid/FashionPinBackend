package com.fashionpin.brandintegrationservice.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {
    private String id;
    private String name;
    private String slug;
    private String description;
    private String logoMediaId;
    private String website;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
