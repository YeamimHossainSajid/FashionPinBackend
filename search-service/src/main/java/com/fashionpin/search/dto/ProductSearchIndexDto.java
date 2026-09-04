package com.fashionpin.search.dto;

import com.fashionpin.search.domain.model.ProductSearchIndex;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchIndexDto {

    private UUID id;
    private UUID brandId;
    private String title;
    private String description;
    private String category;
    private String subcategory;
    private List<String> colors;
    private List<String> sizes;
    private BigDecimal priceAmount;
    private String priceCurrency;
    private List<String> tags;
    private boolean inStock;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProductSearchIndexDto fromEntity(ProductSearchIndex entity) {
        if (entity == null) {
            return null;
        }
        return ProductSearchIndexDto.builder()
                .id(entity.getId())
                .brandId(entity.getBrandId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .subcategory(entity.getSubcategory())
                .colors(entity.getColors())
                .sizes(entity.getSizes())
                .priceAmount(entity.getPriceAmount())
                .priceCurrency(entity.getPriceCurrency())
                .tags(entity.getTags())
                .inStock(entity.isInStock())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
