package com.fashionpin.outfitdetectionservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@Table(name = "detected_items")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectedItem {

    @Id
    private String id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private DetectionResult result;

    @Column(nullable = false)
    private String category;

    private Double confidence;

    @Column(name = "bounding_box_json", columnDefinition = "TEXT")
    private String boundingBoxJson;

    @Column(name = "product_candidate_ids_json", columnDefinition = "TEXT")
    private String productCandidateIdsJson;

    @Column(name = "attributes_json", columnDefinition = "TEXT")
    private String attributesJson;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
