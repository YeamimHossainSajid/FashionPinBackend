package com.fashionpin.search.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchQueryCriteria {

    private String query;
    private UUID brandId;
    private String category;
    private String subcategory;
    private String color;
    private String size;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStock;

    @Builder.Default
    private String sortBy = "RELEVANCE";

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int pageSize = 20;
}
