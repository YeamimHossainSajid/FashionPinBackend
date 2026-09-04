package com.fashionpin.search.dto;

import java.math.BigDecimal;
import java.util.Collections;
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

    @Builder.Default
    private Map<String, Long> brands = Collections.emptyMap();

    @Builder.Default
    private Map<String, Long> categories = Collections.emptyMap();

    @Builder.Default
    private Map<String, Long> colors = Collections.emptyMap();

    @Builder.Default
    private Map<String, Long> sizes = Collections.emptyMap();

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
