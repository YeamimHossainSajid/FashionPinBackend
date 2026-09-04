package com.fashionpin.common.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceRangeFacetDto {
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
