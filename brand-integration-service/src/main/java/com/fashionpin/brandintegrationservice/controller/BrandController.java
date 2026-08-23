package com.fashionpin.brandintegrationservice.controller;

import com.fashionpin.brandintegrationservice.dto.BrandResponse;
import com.fashionpin.brandintegrationservice.dto.CreateBrandRequest;
import com.fashionpin.brandintegrationservice.dto.UpdateBrandRequest;
import com.fashionpin.brandintegrationservice.service.BrandService;
import com.fashionpin.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Tag(name = "Brands", description = "Brand management and catalog integration endpoints")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping({"/api/brands", "/api/v1/brand-integration"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new brand (Admin only)")
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(@Valid @RequestBody CreateBrandRequest request) {
        BrandResponse response = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Brand created successfully", response));
    }

    @GetMapping({"/api/brands/{id}", "/api/v1/brand-integration/{id}"})
    @Operation(summary = "Get brand by ID")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandById(@PathVariable("id") String id) {
        BrandResponse response = brandService.getBrandById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping({"/api/brands", "/api/v1/brand-integration"})
    @Operation(summary = "Get all active brands")
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getAllBrands() {
        List<BrandResponse> response = brandService.getAllBrands();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping({"/api/brands/{id}", "/api/v1/brand-integration/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update brand details (Admin only)")
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateBrandRequest request) {
        BrandResponse response = brandService.updateBrand(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Brand updated successfully", response));
    }

    @DeleteMapping({"/api/brands/{id}", "/api/v1/brand-integration/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete brand (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable("id") String id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.ok("Brand deleted successfully", null));
    }
}
