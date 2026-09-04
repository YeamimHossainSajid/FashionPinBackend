package com.fashionpin.search.domain.model;

import com.fashionpin.common.event.ProductEventPayload;
import com.fashionpin.search.domain.model.converter.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_search_index")
public class ProductSearchIndex {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "brand_id", nullable = false)
    private UUID brandId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "subcategory")
    private String subcategory;

    @Convert(converter = StringListConverter.class)
    @Column(name = "colors")
    private List<String> colors;

    @Convert(converter = StringListConverter.class)
    @Column(name = "sizes")
    private List<String> sizes;

    @Column(name = "price_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAmount;

    @Column(name = "price_currency", nullable = false, length = 3)
    private String priceCurrency;

    @Convert(converter = StringListConverter.class)
    @Column(name = "tags")
    private List<String> tags;

    @Column(name = "in_stock", nullable = false)
    private boolean inStock;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static ProductSearchIndex fromPayload(ProductEventPayload payload) {
        if (payload == null) {
            return null;
        }
        Instant now = payload.getTimestamp() != null ? payload.getTimestamp() : Instant.now();
        return ProductSearchIndex.builder()
                .id(payload.getProductId())
                .brandId(payload.getBrandId())
                .title(payload.getTitle())
                .description(payload.getDescription())
                .category(payload.getCategory())
                .subcategory(payload.getSubcategory())
                .colors(payload.getColors())
                .sizes(payload.getSizes())
                .priceAmount(payload.getPrice())
                .priceCurrency(payload.getCurrency() != null ? payload.getCurrency() : "USD")
                .tags(payload.getTags())
                .inStock(payload.isInStock())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateFromPayload(ProductEventPayload payload) {
        if (payload == null) {
            return;
        }
        if (payload.getBrandId() != null) {
            this.brandId = payload.getBrandId();
        }
        if (payload.getTitle() != null) {
            this.title = payload.getTitle();
        }
        if (payload.getDescription() != null) {
            this.description = payload.getDescription();
        }
        if (payload.getCategory() != null) {
            this.category = payload.getCategory();
        }
        if (payload.getSubcategory() != null) {
            this.subcategory = payload.getSubcategory();
        }
        if (payload.getColors() != null) {
            this.colors = payload.getColors();
        }
        if (payload.getSizes() != null) {
            this.sizes = payload.getSizes();
        }
        if (payload.getPrice() != null) {
            this.priceAmount = payload.getPrice();
        }
        if (payload.getCurrency() != null) {
            this.priceCurrency = payload.getCurrency();
        }
        if (payload.getTags() != null) {
            this.tags = payload.getTags();
        }
        this.inStock = payload.isInStock();
        this.updatedAt = payload.getTimestamp() != null ? payload.getTimestamp() : Instant.now();
    }
}
