package com.fashionpin.productservice.service;

import com.fashionpin.productservice.dto.StorefrontConfigResponse;
import com.fashionpin.productservice.dto.StorefrontConfigResponse.StorefrontCuratedCollection;
import com.fashionpin.productservice.dto.StorefrontConfigResponse.StorefrontNavCategory;
import com.fashionpin.productservice.dto.StorefrontConfigResponse.StorefrontNavFeaturedCollection;
import com.fashionpin.productservice.dto.StorefrontConfigResponse.StorefrontNavItemType;
import com.fashionpin.productservice.entity.Category;
import com.fashionpin.productservice.entity.Collection;
import com.fashionpin.productservice.repository.CategoryRepository;
import com.fashionpin.productservice.repository.CollectionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorefrontConfigService {

    private final CategoryRepository categoryRepository;
    private final CollectionRepository collectionRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "storefront_config", key = "'global'", unless = "#result == null")
    public StorefrontConfigResponse getStorefrontConfig() {
        log.info("Generating storefront config payload");

        List<Category> categories = categoryRepository.findByParentIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
        List<Collection> featuredCollections = collectionRepository.findByIsFeaturedTrueAndIsActiveTrueOrderByDisplayOrderAsc();
        List<Collection> allActiveCollections = collectionRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        List<StorefrontNavFeaturedCollection> navFeatured = featuredCollections.stream()
                .map(col -> StorefrontNavFeaturedCollection.builder()
                        .name(col.getName())
                        .slug(col.getSlug())
                        .accentColor(col.getAccentColor())
                        .build())
                .toList();

        List<StorefrontNavCategory> navCategories = categories.stream()
                .map(cat -> {
                    List<StorefrontNavItemType> itemTypes = cat.getItemTypes() != null
                            ? cat.getItemTypes().stream()
                                    .filter(it -> Boolean.TRUE.equals(it.getIsActive()))
                                    .map(it -> StorefrontNavItemType.builder()
                                            .id(it.getId())
                                            .name(it.getName())
                                            .slug(it.getSlug())
                                            .build())
                                    .toList()
                            : List.of();

                    return StorefrontNavCategory.builder()
                            .id(cat.getId())
                            .name(cat.getName())
                            .slug(cat.getSlug())
                            .bannerMediaUrl(cat.getBannerMediaUrl())
                            .itemTypes(itemTypes)
                            .featuredCollections(navFeatured)
                            .build();
                })
                .toList();

        List<StorefrontCuratedCollection> curatedCollections = allActiveCollections.stream()
                .map(col -> StorefrontCuratedCollection.builder()
                        .id(col.getId())
                        .name(col.getName())
                        .slug(col.getSlug())
                        .tagline(col.getTagline())
                        .description(col.getDescription())
                        .heroImageUrl(col.getHeroMediaUrl())
                        .accentColor(col.getAccentColor())
                        .isFeatured(col.getIsFeatured())
                        .build())
                .toList();

        return StorefrontConfigResponse.builder()
                .navigation(navCategories)
                .curatedCollections(curatedCollections)
                .build();
    }
}
