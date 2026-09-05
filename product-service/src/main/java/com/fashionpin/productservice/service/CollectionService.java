package com.fashionpin.productservice.service;

import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.productservice.dto.AddCollectionProductRequest;
import com.fashionpin.productservice.dto.CollectionDto;
import com.fashionpin.productservice.dto.CreateCollectionRequest;
import com.fashionpin.productservice.dto.ProductResponse;
import com.fashionpin.productservice.dto.UpdateCollectionRequest;
import com.fashionpin.productservice.entity.Collection;
import com.fashionpin.productservice.entity.CollectionProduct;
import com.fashionpin.productservice.entity.CollectionProduct.CollectionProductId;
import com.fashionpin.productservice.entity.Product;
import com.fashionpin.productservice.repository.CollectionProductRepository;
import com.fashionpin.productservice.repository.CollectionRepository;
import com.fashionpin.productservice.repository.ProductRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionProductRepository collectionProductRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<CollectionDto> getActiveCollections() {
        return collectionRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(col -> mapToDto(col, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CollectionDto> getFeaturedCollections() {
        return collectionRepository.findByIsFeaturedTrueAndIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(col -> mapToDto(col, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public CollectionDto getCollectionBySlug(String slug) {
        Collection collection = collectionRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with slug: " + slug));
        return mapToDto(collection, true);
    }

    @Transactional
    public CollectionDto createCollection(CreateCollectionRequest request) {
        String slug = request.getSlug() != null && !request.getSlug().isBlank()
                ? request.getSlug().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-")
                : request.getName().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-");

        if (collectionRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }

        Collection collection = Collection.builder()
                .name(request.getName())
                .slug(slug)
                .tagline(request.getTagline())
                .description(request.getDescription())
                .heroMediaId(request.getHeroMediaId())
                .heroMediaUrl(request.getHeroMediaUrl())
                .accentColor(request.getAccentColor())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .isActive(true)
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .build();

        Collection saved = collectionRepository.save(collection);
        log.info("Created curated collection: {} ({})", saved.getName(), saved.getSlug());
        return mapToDto(saved, false);
    }

    @Transactional
    public CollectionDto updateCollection(String id, UpdateCollectionRequest request) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + id));

        if (request.getName() != null) collection.setName(request.getName());
        if (request.getSlug() != null) collection.setSlug(request.getSlug().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-"));
        if (request.getTagline() != null) collection.setTagline(request.getTagline());
        if (request.getDescription() != null) collection.setDescription(request.getDescription());
        if (request.getHeroMediaId() != null) collection.setHeroMediaId(request.getHeroMediaId());
        if (request.getHeroMediaUrl() != null) collection.setHeroMediaUrl(request.getHeroMediaUrl());
        if (request.getAccentColor() != null) collection.setAccentColor(request.getAccentColor());
        if (request.getDisplayOrder() != null) collection.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsFeatured() != null) collection.setIsFeatured(request.getIsFeatured());
        if (request.getIsActive() != null) collection.setIsActive(request.getIsActive());
        if (request.getStartsAt() != null) collection.setStartsAt(request.getStartsAt());
        if (request.getEndsAt() != null) collection.setEndsAt(request.getEndsAt());

        Collection updated = collectionRepository.save(collection);
        log.info("Updated collection: {}", updated.getId());
        return mapToDto(updated, false);
    }

    @Transactional
    public void addProductToCollection(String collectionId, AddCollectionProductRequest request) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + collectionId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        CollectionProductId id = new CollectionProductId(collection.getId(), product.getId());
        CollectionProduct collectionProduct = CollectionProduct.builder()
                .id(id)
                .collection(collection)
                .product(product)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .curatorNote(request.getCuratorNote())
                .build();

        collectionProductRepository.save(collectionProduct);
        log.info("Added product {} to collection {}", product.getId(), collection.getId());
    }

    @Transactional
    public void removeProductFromCollection(String collectionId, String productId) {
        collectionProductRepository.deleteByIdCollectionIdAndIdProductId(collectionId, productId);
        log.info("Removed product {} from collection {}", productId, collectionId);
    }

    @Transactional
    public void deleteCollection(String id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + id));
        collectionRepository.delete(collection);
        log.info("Deleted collection: {}", id);
    }

    public CollectionDto mapToDto(Collection collection, boolean includeProducts) {
        List<ProductResponse> productResponses = List.of();
        if (includeProducts) {
            List<Product> products = productRepository.findByCollectionId(collection.getId());
            productResponses = products.stream()
                    .map(ProductService::toResponse)
                    .toList();
        }

        return CollectionDto.builder()
                .id(collection.getId())
                .name(collection.getName())
                .slug(collection.getSlug())
                .tagline(collection.getTagline())
                .description(collection.getDescription())
                .heroMediaId(collection.getHeroMediaId())
                .heroMediaUrl(collection.getHeroMediaUrl())
                .accentColor(collection.getAccentColor())
                .displayOrder(collection.getDisplayOrder())
                .isFeatured(collection.getIsFeatured())
                .isActive(collection.getIsActive())
                .startsAt(collection.getStartsAt())
                .endsAt(collection.getEndsAt())
                .products(productResponses)
                .build();
    }
}
