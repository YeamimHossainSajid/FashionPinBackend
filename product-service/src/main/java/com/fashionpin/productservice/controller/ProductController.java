package com.fashionpin.productservice.controller;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.productservice.dto.CreateProductRequest;
import com.fashionpin.productservice.dto.ProductResponse;
import com.fashionpin.productservice.dto.UpdateProductRequest;
import com.fashionpin.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Tag(name = "Products", description = "Product catalog management endpoints")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping({"/api/products", "/api/v1/product"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create catalog product (Admin only)")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product created successfully", response));
    }

    @GetMapping({"/api/products/{id}", "/api/v1/product/{id}"})
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable("id") String id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping({"/api/products", "/api/v1/product"})
    @Operation(summary = "Get paginated & filtered products catalog")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "brandId", required = false) String brandId,
            @RequestParam(name = "productType", required = false) String productType,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {

        String[] sortParams = sort.split(",");
        String sortProperty = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortProperty));
        Page<ProductResponse> response = productService.getProducts(category, brandId, productType, status, pageable);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping({"/api/products/{id}", "/api/v1/product/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product details (Admin only)")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", response));
    }

    @DeleteMapping({"/api/products/{id}", "/api/v1/product/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable("id") String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted successfully", null));
    }
}
