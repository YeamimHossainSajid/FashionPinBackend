package com.fashionpin.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String slug;
    private String description;
    private String parentId;
    private Integer displayOrder;
    private String bannerMediaId;
    private String bannerMediaUrl;
}
