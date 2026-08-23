package com.fashionpin.mediaservice.controller;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.mediaservice.dto.MediaResponse;
import com.fashionpin.mediaservice.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
@Tag(name = "Media", description = "Media file management endpoints")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping({"/api/media/upload", "/api/v1/media/upload"})
    @Operation(summary = "Upload media file")
    public ResponseEntity<ApiResponse<MediaResponse>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        String ownerId = principal != null ? principal.getName() : "system";
        MediaResponse response = mediaService.uploadMedia(file, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Media uploaded successfully", response));
    }

    @GetMapping({"/api/media/{id}/metadata", "/api/v1/media/{id}/metadata"})
    @Operation(summary = "Get media metadata by ID")
    public ResponseEntity<ApiResponse<MediaResponse>> getMediaMetadata(@PathVariable("id") String id) {
        MediaResponse response = mediaService.getMediaMetadata(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping({"/api/media/{id}", "/api/v1/media/{id}"})
    @Operation(summary = "Download media content by ID")
    public ResponseEntity<byte[]> getMediaContent(@PathVariable("id") String id) {
        MediaResponse metadata = mediaService.getMediaMetadata(id);
        byte[] data = mediaService.getMediaContent(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(metadata.getContentType() != null ? metadata.getContentType() : "application/octet-stream"))
                .body(data);
    }

    @DeleteMapping({"/api/media/{id}", "/api/v1/media/{id}"})
    @Operation(summary = "Delete media file by ID")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(
            @PathVariable("id") String id,
            Principal principal) {
        String ownerId = principal != null ? principal.getName() : "system";
        mediaService.deleteMedia(id, ownerId);
        return ResponseEntity.ok(ApiResponse.ok("Media deleted successfully", null));
    }
}
