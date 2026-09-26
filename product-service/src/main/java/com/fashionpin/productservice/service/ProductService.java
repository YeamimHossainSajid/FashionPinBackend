package com.fashionpin.productservice.service;

import com.fashionpin.common.event.ProductCreatedEvent;
import com.fashionpin.common.event.ProductDeletedEvent;
import com.fashionpin.common.event.ProductUpdatedEvent;
import com.fashionpin.common.exception.BusinessException;
import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.productservice.client.BrandServiceClient;
import com.fashionpin.productservice.dto.CreateProductRequest;
import com.fashionpin.productservice.dto.CreateProductVariantRequest;
import com.fashionpin.productservice.dto.ProductResponse;
import com.fashionpin.productservice.dto.ProductVariantDto;
import com.fashionpin.productservice.dto.UpdateProductRequest;
import com.fashionpin.productservice.entity.Category;
import com.fashionpin.productservice.entity.ItemType;
import com.fashionpin.productservice.entity.Product;
import com.fashionpin.productservice.entity.ProductVariant;
import com.fashionpin.productservice.repository.CategoryRepository;
import com.fashionpin.productservice.repository.ItemTypeRepository;
import com.fashionpin.productservice.repository.ProductRepository;
import com.fashionpin.productservice.repository.ProductVariantRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
    private final ProductVariantRepository variantRepository;
    private final CategoryRepository categoryRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final BrandServiceClient brandServiceClient;
    private final OutboxService outboxService;

    public ProductService(
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            CategoryRepository categoryRepository,
            ItemTypeRepository itemTypeRepository,
            BrandServiceClient brandServiceClient,
            OutboxService outboxService) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.categoryRepository = categoryRepository;
        this.itemTypeRepository = itemTypeRepository;
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
            generatedSlug = generatedSlug + "-" + UUID.randomUUID().toString().substring(0, 6);
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

        // Attach dynamic category / itemType if matching slug found
        if (request.getCategory() != null) {
            categoryRepository.findFirstBySlug(request.getCategory().toLowerCase().trim()).ifPresent(product::setDynamicCategory);
        }
        if (request.getSubcategory() != null) {
            String subcatSlug = request.getSubcategory().toLowerCase().trim();
            if (product.getDynamicCategory() != null) {
                itemTypeRepository.findFirstByCategoryIdAndSlug(product.getDynamicCategory().getId(), subcatSlug)
                        .or(() -> itemTypeRepository.findFirstBySlug(subcatSlug))
                        .ifPresent(product::setDynamicItemType);
            } else {
                itemTypeRepository.findFirstBySlug(subcatSlug).ifPresent(product::setDynamicItemType);
            }
        }

        Product saved = productRepository.save(product);

        // Create default initial variant
        String defaultSku = (saved.getSlug().toUpperCase().replaceAll("[^A-Z0-9]", "-") + "-DEF").replace("--", "-");
        ProductVariant defaultVariant = ProductVariant.builder()
                .product(saved)
                .sku(defaultSku)
                .colorName(saved.getColor() != null ? saved.getColor() : "Standard")
                .size(saved.getSize() != null ? saved.getSize() : "One Size")
                .priceOverride(saved.getPrice())
                .stockQuantity(100)
                .isActive(true)
                .build();
        variantRepository.save(defaultVariant);

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
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(
            String category,
            String subcategory,
            String collection,
            String brandId,
            String productType,
            String status,
            Pageable pageable) {

        Page<Product> productsPage;
        if (collection != null && !collection.isBlank()) {
            productsPage = productRepository.findByCollectionSlug(collection, pageable);
        } else if (category != null && !category.isBlank() && subcategory != null && !subcategory.isBlank()) {
            productsPage = productRepository.findByDynamicCategorySlugAndItemTypeSlug(category, subcategory, pageable);
        } else if (category != null && !category.isBlank()) {
            productsPage = productRepository.findByDynamicCategorySlug(category, pageable);
            if (productsPage.isEmpty()) {
                productsPage = productRepository.findByCategory(category, pageable);
            }
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
    public ProductVariantDto createVariant(String productId, CreateProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (variantRepository.existsBySku(request.getSku())) {
            throw new BusinessException("VARIANT_SKU_EXISTS", "SKU already exists: " + request.getSku());
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(request.getSku())
                .colorName(request.getColorName())
                .colorHex(request.getColorHex())
                .size(request.getSize())
                .priceOverride(request.getPriceOverride())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .mediaIds(request.getMediaIds())
                .isActive(true)
                .build();

        ProductVariant saved = variantRepository.save(variant);
        return mapVariantToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantDto> getProductVariants(String productId) {
        return variantRepository.findByProductIdAndIsActiveTrue(productId).stream()
                .map(this::mapVariantToDto)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getSubcategory() != null) product.setSubcategory(request.getSubcategory());
        if (request.getProductType() != null) product.setProductType(request.getProductType());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCurrency() != null) product.setCurrency(request.getCurrency());
        if (request.getPrimaryMediaId() != null) product.setPrimaryMediaId(request.getPrimaryMediaId());
        if (request.getMediaIds() != null) product.setMediaIds(String.join(",", request.getMediaIds()));
        if (request.getGender() != null) product.setGender(request.getGender());
        if (request.getColor() != null) product.setColor(request.getColor());
        if (request.getSize() != null) product.setSize(request.getSize());
        if (request.getMaterial() != null) product.setMaterial(request.getMaterial());

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

    public ProductResponse mapToResponse(Product product) {
        return toResponse(product);
    }

    public static ProductResponse toResponse(Product product) {
        List<String> mediaList = product.getMediaIds() != null && !product.getMediaIds().isBlank()
                ? Arrays.asList(product.getMediaIds().split(","))
                : Collections.emptyList();

        List<String> galleryMediaList = product.getGalleryMediaIds() != null && !product.getGalleryMediaIds().isBlank()
                ? Arrays.asList(product.getGalleryMediaIds().split(","))
                : Collections.emptyList();

        List<ProductVariantDto> variantDtos = product.getVariants() != null
                ? product.getVariants().stream()
                        .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
                        .map(v -> ProductVariantDto.builder()
                                .id(v.getId())
                                .productId(product.getId())
                                .sku(v.getSku())
                                .colorName(v.getColorName())
                                .colorHex(v.getColorHex())
                                .size(v.getSize())
                                .priceOverride(v.getPriceOverride())
                                .effectivePrice(v.getPriceOverride() != null ? v.getPriceOverride() : product.getPrice())
                                .stockQuantity(v.getStockQuantity())
                                .mediaIds(v.getMediaIds())
                                .isActive(v.getIsActive())
                                .build())
                        .toList()
                : List.of();

        return ProductResponse.builder()
                .id(product.getId())
                .brandId(product.getBrandId())
                .categoryId(product.getDynamicCategory() != null ? product.getDynamicCategory().getId() : null)
                .categorySlug(product.getDynamicCategory() != null ? product.getDynamicCategory().getSlug() : null)
                .categoryName(product.getDynamicCategory() != null ? product.getDynamicCategory().getName() : null)
                .itemTypeId(product.getDynamicItemType() != null ? product.getDynamicItemType().getId() : null)
                .itemTypeSlug(product.getDynamicItemType() != null ? product.getDynamicItemType().getSlug() : null)
                .itemTypeName(product.getDynamicItemType() != null ? product.getDynamicItemType().getName() : null)
                .name(product.getName())
                .slug(product.getSlug())
                .summary(product.getSummary())
                .description(product.getDescription())
                .category(product.getCategory())
                .subcategory(product.getSubcategory())
                .productType(product.getProductType())
                .status(product.getStatus())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .primaryMediaId(product.getPrimaryMediaId())
                .mediaIds(mediaList)
                .galleryMediaIds(galleryMediaList)
                .gender(product.getGender())
                .color(product.getColor())
                .size(product.getSize())
                .material(product.getMaterial())
                .pattern(product.getPattern())
                .style(product.getStyle())
                .season(product.getSeason())
                .occasion(product.getOccasion())
                .fit(product.getFit())
                .variants(variantDtos)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductVariantDto mapVariantToDto(ProductVariant v) {
        return ProductVariantDto.builder()
                .id(v.getId())
                .productId(v.getProduct() != null ? v.getProduct().getId() : null)
                .sku(v.getSku())
                .colorName(v.getColorName())
                .colorHex(v.getColorHex())
                .size(v.getSize())
                .priceOverride(v.getPriceOverride())
                .effectivePrice(v.getPriceOverride() != null ? v.getPriceOverride() : (v.getProduct() != null ? v.getProduct().getPrice() : null))
                .stockQuantity(v.getStockQuantity())
                .mediaIds(v.getMediaIds())
                .isActive(v.getIsActive())
                .build();
    }
}
