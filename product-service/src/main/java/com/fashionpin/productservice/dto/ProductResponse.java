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
    private String name;
    private String slug;
    private String description;
    private String category;
    private String subcategory;
    private String productType;
    private String status;
    private BigDecimal price;
    private String currency;
    private String primaryMediaId;
    private List<String> mediaIds;

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

    private Instant createdAt;
    private Instant updatedAt;
}
