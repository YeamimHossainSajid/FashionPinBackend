package com.fashionpin.productservice.service;

import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.productservice.dto.CategoryDto;
import com.fashionpin.productservice.dto.CreateCategoryRequest;
import com.fashionpin.productservice.dto.ItemTypeDto;
import com.fashionpin.productservice.dto.UpdateCategoryRequest;
import com.fashionpin.productservice.entity.Category;
import com.fashionpin.productservice.repository.CategoryRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        return mapToDto(category);
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToDto(category);
    }

    @Transactional
    public CategoryDto createCategory(CreateCategoryRequest request) {
        String slug = request.getSlug() != null && !request.getSlug().isBlank()
                ? request.getSlug().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-")
                : request.getName().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-");

        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .parentId(request.getParentId())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .bannerMediaId(request.getBannerMediaId())
                .bannerMediaUrl(request.getBannerMediaUrl())
                .isActive(true)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Created new category: {} ({})", saved.getName(), saved.getSlug());
        return mapToDto(saved);
    }

    @Transactional
    public CategoryDto updateCategory(String id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (request.getName() != null) category.setName(request.getName());
        if (request.getSlug() != null) category.setSlug(request.getSlug().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-"));
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getParentId() != null) category.setParentId(request.getParentId());
        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());
        if (request.getBannerMediaId() != null) category.setBannerMediaId(request.getBannerMediaId());
        if (request.getBannerMediaUrl() != null) category.setBannerMediaUrl(request.getBannerMediaUrl());

        Category updated = categoryRepository.save(category);
        log.info("Updated category: {}", updated.getId());
        return mapToDto(updated);
    }

    @Transactional
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(category);
        log.info("Deleted category: {}", id);
    }

    public CategoryDto mapToDto(Category category) {
        List<ItemTypeDto> itemTypeDtos = category.getItemTypes() != null
                ? category.getItemTypes().stream()
                        .filter(it -> Boolean.TRUE.equals(it.getIsActive()))
                        .map(it -> ItemTypeDto.builder()
                                .id(it.getId())
                                .categoryId(category.getId())
                                .name(it.getName())
                                .slug(it.getSlug())
                                .description(it.getDescription())
                                .displayOrder(it.getDisplayOrder())
                                .isActive(it.getIsActive())
                                .sizeGuideUrl(it.getSizeGuideUrl())
                                .build())
                        .toList()
                : List.of();

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .bannerMediaId(category.getBannerMediaId())
                .bannerMediaUrl(category.getBannerMediaUrl())
                .itemTypes(itemTypeDtos)
                .build();
    }
}
