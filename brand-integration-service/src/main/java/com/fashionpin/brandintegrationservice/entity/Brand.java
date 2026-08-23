package com.fashionpin.brandintegrationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "brands")
public class Brand {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_media_id")
    private String logoMediaId;

    private String website;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Brand create(String name, String slug, String description, String logoMediaId, String website, String status) {
        Instant now = Instant.now();
        return Brand.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .slug(slug.toLowerCase().trim().replaceAll("[^a-z0-9-]", "-"))
                .description(description)
                .logoMediaId(logoMediaId)
                .website(website)
                .status(status != null ? status : "ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
