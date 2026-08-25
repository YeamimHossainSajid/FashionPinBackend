package com.fashionpin.fashiondiscoveryservice.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "media-service", fallbackFactory = MediaClientFallbackFactory.class)
public interface MediaClient {

    @GetMapping("/api/media/{id}")
    Map<String, Object> getMediaById(@PathVariable("id") String id);
}
