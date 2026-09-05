package com.fashionpin.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    private String name;
    private String slug;
    private String description;
    private String parentId;
    private Integer displayOrder;
    private Boolean isActive;
    private String bannerMediaId;
    private String bannerMediaUrl;
}
