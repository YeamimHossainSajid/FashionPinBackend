package com.fashionpin.productservice.service;

import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.productservice.dto.CreateItemTypeRequest;
import com.fashionpin.productservice.dto.ItemTypeDto;
import com.fashionpin.productservice.dto.UpdateItemTypeRequest;
import com.fashionpin.productservice.entity.Category;
import com.fashionpin.productservice.entity.ItemType;
import com.fashionpin.productservice.repository.CategoryRepository;
import com.fashionpin.productservice.repository.ItemTypeRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemTypeService {

    private final ItemTypeRepository itemTypeRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<ItemTypeDto> getItemTypesByCategory(String categoryIdOrSlug) {
        Category category = categoryRepository.findBySlug(categoryIdOrSlug)
                .or(() -> categoryRepository.findById(categoryIdOrSlug))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryIdOrSlug));

        return itemTypeRepository.findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(category.getId()).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemTypeDto getItemTypeById(String id) {
        ItemType itemType = itemTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ItemType not found with id: " + id));
        return mapToDto(itemType);
    }

    @Transactional
    public ItemTypeDto createItemType(CreateItemTypeRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        String slug = request.getSlug() != null && !request.getSlug().isBlank()
                ? request.getSlug().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-")
                : request.getName().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-");

        if (itemTypeRepository.existsByCategoryIdAndSlug(category.getId(), slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }

        ItemType itemType = ItemType.builder()
                .category(category)
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .sizeGuideUrl(request.getSizeGuideUrl())
                .isActive(true)
                .build();

        ItemType saved = itemTypeRepository.save(itemType);
        log.info("Created item type: {} under category {}", saved.getName(), category.getName());
        return mapToDto(saved);
    }

    @Transactional
    public ItemTypeDto updateItemType(String id, UpdateItemTypeRequest request) {
        ItemType itemType = itemTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ItemType not found with id: " + id));

        if (request.getName() != null) itemType.setName(request.getName());
        if (request.getSlug() != null) itemType.setSlug(request.getSlug().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-"));
        if (request.getDescription() != null) itemType.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) itemType.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) itemType.setIsActive(request.getIsActive());
        if (request.getSizeGuideUrl() != null) itemType.setSizeGuideUrl(request.getSizeGuideUrl());

        ItemType updated = itemTypeRepository.save(itemType);
        log.info("Updated item type: {}", updated.getId());
        return mapToDto(updated);
    }

    @Transactional
    public void deleteItemType(String id) {
        ItemType itemType = itemTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ItemType not found with id: " + id));
        itemTypeRepository.delete(itemType);
        log.info("Deleted item type: {}", id);
    }

    public ItemTypeDto mapToDto(ItemType itemType) {
        return ItemTypeDto.builder()
                .id(itemType.getId())
                .categoryId(itemType.getCategory() != null ? itemType.getCategory().getId() : null)
                .name(itemType.getName())
                .slug(itemType.getSlug())
                .description(itemType.getDescription())
                .displayOrder(itemType.getDisplayOrder())
                .isActive(itemType.getIsActive())
                .sizeGuideUrl(itemType.getSizeGuideUrl())
                .build();
    }
}
