package com.fashionpin.productservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "collection_products")
public class CollectionProduct {

    @EmbeddedId
    private CollectionProductId id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("collectionId")
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder.Default
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "curator_note")
    private String curatorNote;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class CollectionProductId implements Serializable {
        @Column(name = "collection_id")
        private String collectionId;

        @Column(name = "product_id")
        private String productId;
    }
}
