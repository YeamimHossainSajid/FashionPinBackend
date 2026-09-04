package com.fashionpin.common.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFacetResultDto {
    private Map<String, Long> brands;
    private Map<String, Long> categories;
    private Map<String, Long> styles;
    private Map<String, Long> occasions;
    private Map<String, Long> colors;
    private Map<String, Long> sizes;
    private PriceRangeFacetDto priceRange;
}
