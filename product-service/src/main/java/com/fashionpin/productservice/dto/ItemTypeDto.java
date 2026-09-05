package com.fashionpin.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeDto {
    private String id;
    private String categoryId;
    private String name;
    private String slug;
    private String description;
    private Integer displayOrder;
    private Boolean isActive;
    private String sizeGuideUrl;
}
