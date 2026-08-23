package com.fashionpin.mediaservice.entity;

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
@Table(name = "media")
public class Media {

    @Id
    private String id;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    private Integer width;
    private Integer height;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Media create(String ownerId, String mediaType, String storageKey, String originalFilename, String contentType, long fileSize) {
        Instant now = Instant.now();
        return Media.builder()
                .id(UUID.randomUUID().toString())
                .ownerId(ownerId)
                .mediaType(mediaType)
                .storageKey(storageKey)
                .originalFilename(originalFilename)
                .contentType(contentType)
                .fileSize(fileSize)
                .status("READY")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
