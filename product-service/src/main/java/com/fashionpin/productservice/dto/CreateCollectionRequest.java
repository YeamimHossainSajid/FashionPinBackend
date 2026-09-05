package com.fashionpin.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCollectionRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String slug;
    private String tagline;
    private String description;
    private String heroMediaId;
    private String heroMediaUrl;
    private String accentColor;
    private Integer displayOrder;
    private Boolean isFeatured;
    private Instant startsAt;
    private Instant endsAt;
}
