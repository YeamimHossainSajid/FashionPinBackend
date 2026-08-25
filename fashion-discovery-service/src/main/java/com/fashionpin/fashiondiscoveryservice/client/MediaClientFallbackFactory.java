package com.fashionpin.fashiondiscoveryservice.client;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MediaClientFallbackFactory implements FallbackFactory<MediaClient> {

    @Override
    public MediaClient create(Throwable cause) {
        log.warn("MediaClient fallback triggered due to: {}", cause.getMessage());
        return id -> Map.of("id", id, "status", "UNKNOWN");
    }
}
