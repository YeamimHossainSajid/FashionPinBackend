package com.fashionpin.productservice.client;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.productservice.dto.BrandDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "brand-integration-service", configuration = FeignClientConfig.class)
public interface BrandServiceClient {

    @GetMapping("/api/brands/{id}")
    ApiResponse<BrandDto> getBrandById(@PathVariable("id") String id);
}
