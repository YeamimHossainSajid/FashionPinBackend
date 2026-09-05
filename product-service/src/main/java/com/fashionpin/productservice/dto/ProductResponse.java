package com.fashionpin.productservice.dto;

import java.math.BigDecimal;
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
public class ProductResponse {
    private String id;
    private String brandId;
    private String categoryId;
    private String categorySlug;
    private String categoryName;
    private String itemTypeId;
    private String itemTypeSlug;
    private String itemTypeName;
    private String name;
    private String slug;
    private String summary;
    private String description;
    private String category;
    private String subcategory;
    private String productType;
    private String status;
    private BigDecimal price;
    private String currency;
    private String primaryMediaId;
    private List<String> mediaIds;
    private List<String> galleryMediaIds;

    // Fashion Attributes
    private String gender;
    private String color;
    private String size;
    private String material;
    private String pattern;
    private String style;
    private String season;
    private String occasion;
    private String fit;
    private List<String> tags;

    // Variants (SKUs, Colors, Sizes, Stock)
    private List<ProductVariantDto> variants;

    private Instant createdAt;
    private Instant updatedAt;
}
