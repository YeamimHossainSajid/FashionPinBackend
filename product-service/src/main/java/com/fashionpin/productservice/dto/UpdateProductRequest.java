package com.fashionpin.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    private String name;
    private String description;
    private String category;
    private String subcategory;
    private String productType;
    private String status;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
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
}
