package com.fashionpin.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductVariantRequest {

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Color name is required")
    private String colorName;

    private String colorHex;

    @NotBlank(message = "Size is required")
    private String size;

    private BigDecimal priceOverride;
    private Integer stockQuantity;
    private String mediaIds;
}
