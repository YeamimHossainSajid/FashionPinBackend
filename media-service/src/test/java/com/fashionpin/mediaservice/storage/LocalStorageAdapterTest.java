package com.fashionpin.mediaservice.storage;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalStorageAdapterTest {

    @TempDir
    File tempDir;

    private LocalStorageAdapter storageAdapter;

    @BeforeEach
    void setUp() {
        storageAdapter = new LocalStorageAdapter(tempDir.getAbsolutePath());
    }

    @Test
    void uploadAndDownloadAndDelete_Success() {
        String key = "test_image.png";
        byte[] content = "fake image content".getBytes();

        String path = storageAdapter.uploadFile(key, new ByteArrayInputStream(content), "image/png", content.length);
        assertNotNull(path);

        byte[] downloaded = storageAdapter.downloadFile(key);
        assertArrayEquals(content, downloaded);

        storageAdapter.deleteFile(key);
        assertThrows(RuntimeException.class, () -> storageAdapter.downloadFile(key));
    }
}
