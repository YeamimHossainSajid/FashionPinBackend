package com.fashionpin.productservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    private String id;

    @Column(name = "brand_id", nullable = false)
    private String brandId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    private String subcategory;

    @Column(name = "product_type")
    private String productType;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "primary_media_id")
    private String primaryMediaId;

    @Column(name = "media_ids", columnDefinition = "TEXT")
    private String mediaIds;

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

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Product create(
            String brandId,
            String name,
            String slug,
            String description,
            String category,
            String subcategory,
            String productType,
            BigDecimal price,
            String currency,
            String primaryMediaId,
            String mediaIds,
            String gender,
            String color,
            String size,
            String material,
            String pattern,
            String style,
            String season,
            String occasion,
            String fit) {
        Instant now = Instant.now();
        return Product.builder()
                .id(UUID.randomUUID().toString())
                .brandId(brandId)
                .name(name)
                .slug(slug != null ? slug.toLowerCase().trim().replaceAll("[^a-z0-9-]", "-") : UUID.randomUUID().toString())
                .description(description)
                .category(category)
                .subcategory(subcategory)
                .productType(productType)
                .status("ACTIVE")
                .price(price)
                .currency(currency != null ? currency : "USD")
                .primaryMediaId(primaryMediaId)
                .mediaIds(mediaIds)
                .gender(gender)
                .color(color)
                .size(size)
                .material(material)
                .pattern(pattern)
                .style(style)
                .season(season)
                .occasion(occasion)
                .fit(fit)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
