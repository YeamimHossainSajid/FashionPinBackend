package com.fashionpin.productservice.controller;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.productservice.dto.StorefrontConfigResponse;
import com.fashionpin.productservice.service.StorefrontConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Storefront", description = "Storefront dynamic configuration and navigation")
public class StorefrontConfigController {

    private final StorefrontConfigService storefrontConfigService;

    @GetMapping({"/api/storefront/config", "/api/v1/storefront/config"})
    @Operation(summary = "Get dynamic storefront configuration (Navigation, ItemTypes, Curated Collections)")
    public ResponseEntity<ApiResponse<StorefrontConfigResponse>> getStorefrontConfig() {
        StorefrontConfigResponse response = storefrontConfigService.getStorefrontConfig();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
