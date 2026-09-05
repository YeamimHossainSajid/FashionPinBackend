package com.fashionpin.productservice.controller;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.productservice.dto.AddCollectionProductRequest;
import com.fashionpin.productservice.dto.CollectionDto;
import com.fashionpin.productservice.dto.CreateCollectionRequest;
import com.fashionpin.productservice.dto.UpdateCollectionRequest;
import com.fashionpin.productservice.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Tag(name = "Collections", description = "Aesthetic and curated style collection endpoints")
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping({"/api/collections", "/api/v1/collections"})
    @Operation(summary = "Get all active style collections")
    public ResponseEntity<ApiResponse<List<CollectionDto>>> getCollections() {
        return ResponseEntity.ok(ApiResponse.ok(collectionService.getActiveCollections()));
    }

    @GetMapping({"/api/collections/featured", "/api/v1/collections/featured"})
    @Operation(summary = "Get featured style collections")
    public ResponseEntity<ApiResponse<List<CollectionDto>>> getFeaturedCollections() {
        return ResponseEntity.ok(ApiResponse.ok(collectionService.getFeaturedCollections()));
    }

    @GetMapping({"/api/collections/{slug}", "/api/v1/collections/{slug}"})
    @Operation(summary = "Get collection by slug with curated products")
    public ResponseEntity<ApiResponse<CollectionDto>> getCollectionBySlug(@PathVariable("slug") String slug) {
        return ResponseEntity.ok(ApiResponse.ok(collectionService.getCollectionBySlug(slug)));
    }

    // --- Admin Endpoints ---

    @PostMapping({"/api/admin/collections", "/api/v1/admin/collections"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create collection (Admin only)")
    public ResponseEntity<ApiResponse<CollectionDto>> createCollection(@Valid @RequestBody CreateCollectionRequest request) {
        CollectionDto response = collectionService.createCollection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Collection created successfully", response));
    }

    @PatchMapping({"/api/admin/collections/{id}", "/api/v1/admin/collections/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update collection (Admin only)")
    public ResponseEntity<ApiResponse<CollectionDto>> updateCollection(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateCollectionRequest request) {
        CollectionDto response = collectionService.updateCollection(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Collection updated successfully", response));
    }

    @DeleteMapping({"/api/admin/collections/{id}", "/api/v1/admin/collections/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete collection (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteCollection(@PathVariable("id") String id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.ok(ApiResponse.ok("Collection deleted successfully", null));
    }

    @PostMapping({"/api/admin/collections/{id}/products", "/api/v1/admin/collections/{id}/products"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add product to collection (Admin only)")
    public ResponseEntity<ApiResponse<Void>> addProductToCollection(
            @PathVariable("id") String id,
            @Valid @RequestBody AddCollectionProductRequest request) {
        collectionService.addProductToCollection(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Product added to collection", null));
    }

    @DeleteMapping({"/api/admin/collections/{id}/products/{productId}", "/api/v1/admin/collections/{id}/products/{productId}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove product from collection (Admin only)")
    public ResponseEntity<ApiResponse<Void>> removeProductFromCollection(
            @PathVariable("id") String id,
            @PathVariable("productId") String productId) {
        collectionService.removeProductFromCollection(id, productId);
        return ResponseEntity.ok(ApiResponse.ok("Product removed from collection", null));
    }
}
