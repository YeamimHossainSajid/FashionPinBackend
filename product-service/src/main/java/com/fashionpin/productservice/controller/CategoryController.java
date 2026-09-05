package com.fashionpin.productservice.controller;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.productservice.dto.CategoryDto;
import com.fashionpin.productservice.dto.CreateCategoryRequest;
import com.fashionpin.productservice.dto.CreateItemTypeRequest;
import com.fashionpin.productservice.dto.ItemTypeDto;
import com.fashionpin.productservice.dto.UpdateCategoryRequest;
import com.fashionpin.productservice.dto.UpdateItemTypeRequest;
import com.fashionpin.productservice.service.CategoryService;
import com.fashionpin.productservice.service.ItemTypeService;
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
@Tag(name = "Categories", description = "Dynamic category & item-type catalog endpoints")
public class CategoryController {

    private final CategoryService categoryService;
    private final ItemTypeService itemTypeService;

    @GetMapping({"/api/categories", "/api/v1/categories"})
    @Operation(summary = "Get all active categories with their item types")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getActiveCategories()));
    }

    @GetMapping({"/api/categories/{slug}", "/api/v1/categories/{slug}"})
    @Operation(summary = "Get category by slug")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryBySlug(@PathVariable("slug") String slug) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getCategoryBySlug(slug)));
    }

    @GetMapping({"/api/categories/{slug}/item-types", "/api/v1/categories/{slug}/item-types"})
    @Operation(summary = "Get item types under a category")
    public ResponseEntity<ApiResponse<List<ItemTypeDto>>> getItemTypesByCategory(@PathVariable("slug") String slug) {
        return ResponseEntity.ok(ApiResponse.ok(itemTypeService.getItemTypesByCategory(slug)));
    }

    // --- Admin Endpoints ---

    @PostMapping({"/api/admin/categories", "/api/v1/admin/categories"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create category (Admin only)")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryDto response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Category created successfully", response));
    }

    @PatchMapping({"/api/admin/categories/{id}", "/api/v1/admin/categories/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category (Admin only)")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryDto response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Category updated successfully", response));
    }

    @DeleteMapping({"/api/admin/categories/{id}", "/api/v1/admin/categories/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable("id") String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted successfully", null));
    }

    @PostMapping({"/api/admin/item-types", "/api/v1/admin/item-types"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create item type under category (Admin only)")
    public ResponseEntity<ApiResponse<ItemTypeDto>> createItemType(@Valid @RequestBody CreateItemTypeRequest request) {
        ItemTypeDto response = itemTypeService.createItemType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("ItemType created successfully", response));
    }

    @PatchMapping({"/api/admin/item-types/{id}", "/api/v1/admin/item-types/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update item type (Admin only)")
    public ResponseEntity<ApiResponse<ItemTypeDto>> updateItemType(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateItemTypeRequest request) {
        ItemTypeDto response = itemTypeService.updateItemType(id, request);
        return ResponseEntity.ok(ApiResponse.ok("ItemType updated successfully", response));
    }

    @DeleteMapping({"/api/admin/item-types/{id}", "/api/v1/admin/item-types/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete item type (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteItemType(@PathVariable("id") String id) {
        itemTypeService.deleteItemType(id);
        return ResponseEntity.ok(ApiResponse.ok("ItemType deleted successfully", null));
    }
}
