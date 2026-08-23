package com.fashionpin.mediaservice.service;

import com.fashionpin.common.event.MediaCreatedEvent;
import com.fashionpin.common.event.MediaDeletedEvent;
import com.fashionpin.common.event.MediaReadyEvent;
import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.mediaservice.dto.MediaResponse;
import com.fashionpin.mediaservice.entity.Media;
import com.fashionpin.mediaservice.port.StoragePort;
import com.fashionpin.mediaservice.repository.MediaRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final StoragePort storagePort;
    private final OutboxService outboxService;

    public MediaService(
            MediaRepository mediaRepository,
            StoragePort storagePort,
            OutboxService outboxService) {
        this.mediaRepository = mediaRepository;
        this.storagePort = storagePort;
        this.outboxService = outboxService;
    }

    @Transactional
    public MediaResponse uploadMedia(MultipartFile file, String ownerId) {
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String mediaType = contentType.startsWith("video/") ? "VIDEO" : "IMAGE";

        String storageKey = UUID.randomUUID() + "_" + (originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_") : "file");

        try {
            storagePort.uploadFile(storageKey, file.getInputStream(), contentType, file.getSize());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file input stream", e);
        }

        Media media = Media.create(ownerId, mediaType, storageKey, originalFilename, contentType, file.getSize());
        Media saved = mediaRepository.save(media);

        MediaCreatedEvent createdEvent = MediaCreatedEvent.create(saved.getId(), ownerId, mediaType, storageKey, null);
        outboxService.saveEvent("Media", saved.getId(), "MediaCreated", createdEvent);

        MediaReadyEvent readyEvent = MediaReadyEvent.create(saved.getId(), ownerId, null);
        outboxService.saveEvent("Media", saved.getId(), "MediaReady", readyEvent);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public MediaResponse getMediaMetadata(String id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
        return mapToResponse(media);
    }

    @Transactional(readOnly = true)
    public byte[] getMediaContent(String id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
        return storagePort.downloadFile(media.getStorageKey());
    }

    @Transactional
    public void deleteMedia(String id, String ownerId) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));

        media.setStatus("DELETED");
        media.setUpdatedAt(Instant.now());
        mediaRepository.save(media);

        storagePort.deleteFile(media.getStorageKey());

        MediaDeletedEvent deletedEvent = MediaDeletedEvent.create(id, ownerId, null);
        outboxService.saveEvent("Media", id, "MediaDeleted", deletedEvent);
    }

    private MediaResponse mapToResponse(Media media) {
        String downloadUrl = storagePort.generatePreSignedUrl(media.getStorageKey(), 60);
        return MediaResponse.builder()
                .id(media.getId())
                .ownerId(media.getOwnerId())
                .mediaType(media.getMediaType())
                .storageKey(media.getStorageKey())
                .originalFilename(media.getOriginalFilename())
                .contentType(media.getContentType())
                .fileSize(media.getFileSize())
                .width(media.getWidth())
                .height(media.getHeight())
                .status(media.getStatus())
                .downloadUrl(downloadUrl)
                .createdAt(media.getCreatedAt())
                .updatedAt(media.getUpdatedAt())
                .build();
    }
}
