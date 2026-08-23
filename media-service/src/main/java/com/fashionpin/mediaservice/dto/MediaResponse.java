package com.fashionpin.mediaservice.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponse {
    private String id;
    private String ownerId;
    private String mediaType;
    private String storageKey;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private Integer width;
    private Integer height;
    private String status;
    private String downloadUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
