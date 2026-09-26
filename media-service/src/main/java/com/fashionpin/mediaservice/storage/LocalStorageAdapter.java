package com.fashionpin.mediaservice.storage;

import com.fashionpin.mediaservice.port.StoragePort;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
public class LocalStorageAdapter implements StoragePort {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageAdapter.class);

    private final File storageDir;

    public LocalStorageAdapter(
            @Value("${media.storage.directory:./storage/media}") String storagePath) {
        this.storageDir = new File(storagePath);
        if (!storageDir.exists()) {
            boolean created = storageDir.mkdirs();
            if (created) {
                log.info("Created local media storage directory: {}", storageDir.getAbsolutePath());
            }
        }
    }

    @Override
    public String uploadFile(String key, InputStream inputStream, String contentType, long length) {
        File targetFile = new File(storageDir, key.replaceAll("[^a-zA-Z0-9._-]", "_"));
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            inputStream.transferTo(fos);
            log.info("Saved local media file key={} size={} bytes to {}", key, targetFile.length(), targetFile.getAbsolutePath());
            return targetFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save media file to local storage", e);
        }
    }

    @Override
    public byte[] downloadFile(String key) {
        File file = new File(storageDir, key.replaceAll("[^a-zA-Z0-9._-]", "_"));
        if (!file.exists()) {
            throw new RuntimeException("Media file not found key: " + key);
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read media file key: " + key, e);
        }
    }

    @Override
    public void deleteFile(String key) {
        File file = new File(storageDir, key.replaceAll("[^a-zA-Z0-9._-]", "_"));
        if (file.exists()) {
            boolean deleted = file.delete();
            log.info("Deleted local media file key={}: success={}", key, deleted);
        }
    }

    @Override
    public String generatePreSignedUrl(String key, long expirationMinutes) {
        return "/api/media/download/" + key;
    }
}
