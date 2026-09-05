package com.fashionpin.productservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private String id;
    private String name;
    private String slug;
    private String description;
    private String parentId;
    private Integer displayOrder;
    private Boolean isActive;
    private String bannerMediaId;
    private String bannerMediaUrl;
    private List<ItemTypeDto> itemTypes;
}
