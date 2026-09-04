package com.fashionpin.common.event;

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
public class ProductEventPayload {
    private UUID productId;
    private UUID brandId;
    private String title;
    private String description;
    private String category;
    private String subcategory;
    private List<String> colors;
    private List<String> sizes;
    private BigDecimal price;
    private String currency;
    private List<String> tags;
    private boolean inStock;
    private Instant timestamp;
}
