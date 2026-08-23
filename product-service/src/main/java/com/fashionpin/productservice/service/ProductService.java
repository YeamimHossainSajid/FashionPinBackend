package com.fashionpin.productservice.service;

import com.fashionpin.common.event.ProductCreatedEvent;
import com.fashionpin.common.event.ProductDeletedEvent;
import com.fashionpin.common.event.ProductUpdatedEvent;
import com.fashionpin.common.exception.BusinessException;
import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.productservice.client.BrandServiceClient;
import com.fashionpin.productservice.dto.CreateProductRequest;
import com.fashionpin.productservice.dto.ProductResponse;
import com.fashionpin.productservice.dto.UpdateProductRequest;
import com.fashionpin.productservice.entity.Product;
import com.fashionpin.productservice.repository.ProductRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final BrandServiceClient brandServiceClient;
    private final OutboxService outboxService;

    public ProductService(
            ProductRepository productRepository,
            BrandServiceClient brandServiceClient,
            OutboxService outboxService) {
        this.productRepository = productRepository;
        this.brandServiceClient = brandServiceClient;
        this.outboxService = outboxService;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        validateBrand(request.getBrandId());

        String generatedSlug = request.getSlug() != null && !request.getSlug().isBlank()
                ? request.getSlug().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-")
                : request.getName().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-");

        if (productRepository.existsBySlug(generatedSlug)) {
            throw new BusinessException("PRODUCT_SLUG_ALREADY_EXISTS", "Product slug already exists: " + generatedSlug);
        }

        String mediaIdsStr = request.getMediaIds() != null ? String.join(",", request.getMediaIds()) : null;

        Product product = Product.create(
                request.getBrandId(),
                request.getName(),
                generatedSlug,
                request.getDescription(),
                request.getCategory(),
                request.getSubcategory(),
                request.getProductType(),
                request.getPrice(),
                request.getCurrency(),
                request.getPrimaryMediaId(),
                mediaIdsStr,
                request.getGender(),
                request.getColor(),
                request.getSize(),
                request.getMaterial(),
                request.getPattern(),
                request.getStyle(),
                request.getSeason(),
                request.getOccasion(),
                request.getFit()
        );

        Product saved = productRepository.save(product);

        ProductCreatedEvent createdEvent = ProductCreatedEvent.create(saved.getId(), saved.getBrandId(), saved.getName(), saved.getCategory(), null);
        outboxService.saveEvent("Product", saved.getId(), "ProductCreated", createdEvent);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(
            String category,
            String brandId,
            String productType,
            String status,
            Pageable pageable) {

        Page<Product> productsPage;
        if (category != null && !category.isBlank()) {
            productsPage = productRepository.findByCategory(category, pageable);
        } else if (brandId != null && !brandId.isBlank()) {
            productsPage = productRepository.findByBrandId(brandId, pageable);
        } else if (status != null && !status.isBlank()) {
            productsPage = productRepository.findByStatus(status, pageable);
        } else {
            productsPage = productRepository.findAll(pageable);
        }

        return productsPage.map(this::mapToResponse);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getSubcategory() != null) {
            product.setSubcategory(request.getSubcategory());
        }
        if (request.getProductType() != null) {
            product.setProductType(request.getProductType());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCurrency() != null) {
            product.setCurrency(request.getCurrency());
        }
        if (request.getPrimaryMediaId() != null) {
            product.setPrimaryMediaId(request.getPrimaryMediaId());
        }
        if (request.getMediaIds() != null) {
            product.setMediaIds(String.join(",", request.getMediaIds()));
        }
        if (request.getGender() != null) {
            product.setGender(request.getGender());
        }
        if (request.getColor() != null) {
            product.setColor(request.getColor());
        }
        if (request.getSize() != null) {
            product.setSize(request.getSize());
        }
        if (request.getMaterial() != null) {
            product.setMaterial(request.getMaterial());
        }

        product.setUpdatedAt(Instant.now());
        Product updated = productRepository.save(product);

        ProductUpdatedEvent updatedEvent = ProductUpdatedEvent.create(updated.getId(), updated.getBrandId(), updated.getName(), null);
        outboxService.saveEvent("Product", updated.getId(), "ProductUpdated", updatedEvent);

        return mapToResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productRepository.delete(product);

        ProductDeletedEvent deletedEvent = ProductDeletedEvent.create(id, product.getBrandId(), null);
        outboxService.saveEvent("Product", id, "ProductDeleted", deletedEvent);
    }

    private void validateBrand(String brandId) {
        try {
            var brandResponse = brandServiceClient.getBrandById(brandId);
            if (brandResponse == null || brandResponse.getData() == null) {
                log.warn("Brand validation returned empty response for brandId: {}", brandId);
            }
        } catch (Exception e) {
            log.warn("Brand validation via Feign skipped or unavailable for brandId={}: {}", brandId, e.getMessage());
        }
    }

    private ProductResponse mapToResponse(Product product) {
        List<String> mediaList = product.getMediaIds() != null && !product.getMediaIds().isBlank()
                ? Arrays.asList(product.getMediaIds().split(","))
                : Collections.emptyList();

        return ProductResponse.builder()
                .id(product.getId())
                .brandId(product.getBrandId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .category(product.getCategory())
                .subcategory(product.getSubcategory())
                .productType(product.getProductType())
                .status(product.getStatus())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .primaryMediaId(product.getPrimaryMediaId())
                .mediaIds(mediaList)
                .gender(product.getGender())
                .color(product.getColor())
                .size(product.getSize())
                .material(product.getMaterial())
                .pattern(product.getPattern())
                .style(product.getStyle())
                .season(product.getSeason())
                .occasion(product.getOccasion())
                .fit(product.getFit())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
