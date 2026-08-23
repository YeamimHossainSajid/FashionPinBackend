package com.fashionpin.mediaservice.port;

import java.io.InputStream;

public interface StoragePort {
    String uploadFile(String key, InputStream inputStream, String contentType, long length);
    byte[] downloadFile(String key);
    void deleteFile(String key);
    String generatePreSignedUrl(String key, long expirationMinutes);
}
