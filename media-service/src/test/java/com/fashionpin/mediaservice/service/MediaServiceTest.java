package com.fashionpin.mediaservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fashionpin.mediaservice.dto.MediaResponse;
import com.fashionpin.mediaservice.entity.Media;
import com.fashionpin.mediaservice.port.StoragePort;
import com.fashionpin.mediaservice.repository.MediaRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private StoragePort storagePort;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private MediaService mediaService;

    private Media sampleMedia;

    @BeforeEach
    void setUp() {
        sampleMedia = Media.create("user-1", "IMAGE", "storage-key-1", "fashion.jpg", "image/jpeg", 1024);
    }

    @Test
    void uploadMedia_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "fashion.jpg", "image/jpeg", "image data".getBytes());

        when(storagePort.uploadFile(any(), any(), any(), anyLong())).thenReturn("/path/to/file");
        when(mediaRepository.save(any(Media.class))).thenAnswer(i -> i.getArgument(0));

        MediaResponse response = mediaService.uploadMedia(file, "user-1");

        assertNotNull(response);
        assertEquals("user-1", response.getOwnerId());
        assertEquals("IMAGE", response.getMediaType());
        verify(outboxService, times(2)).saveEvent(any(), any(), any(), any());
    }

    @Test
    void getMediaMetadata_Success() {
        when(mediaRepository.findById(sampleMedia.getId())).thenReturn(Optional.of(sampleMedia));
        when(storagePort.generatePreSignedUrl(any(), anyLong())).thenReturn("http://download.url");

        MediaResponse response = mediaService.getMediaMetadata(sampleMedia.getId());

        assertNotNull(response);
        assertEquals(sampleMedia.getId(), response.getId());
        assertEquals("http://download.url", response.getDownloadUrl());
    }

    @Test
    void deleteMedia_Success() {
        when(mediaRepository.findById(sampleMedia.getId())).thenReturn(Optional.of(sampleMedia));

        mediaService.deleteMedia(sampleMedia.getId(), "user-1");

        verify(storagePort, times(1)).deleteFile(sampleMedia.getStorageKey());
        verify(outboxService, times(1)).saveEvent(eq("Media"), eq(sampleMedia.getId()), eq("MediaDeleted"), any());
    }
}
