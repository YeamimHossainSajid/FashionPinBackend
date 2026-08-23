package com.fashionpin.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateProductRequest {

    @NotBlank(message = "Brand ID is required")
    private String brandId;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    private String name;

    private String slug;
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    private String subcategory;
    private String productType;

    @NotNull(message = "Price is required")
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
