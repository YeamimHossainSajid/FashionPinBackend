package com.fashionpin.productservice.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDto {
    private String id;
    private String productId;
    private String sku;
    private String colorName;
    private String colorHex;
    private String size;
    private BigDecimal priceOverride;
    private BigDecimal effectivePrice;
    private Integer stockQuantity;
    private String mediaIds;
    private Boolean isActive;
}
