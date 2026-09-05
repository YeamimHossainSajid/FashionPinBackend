package com.fashionpin.productservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorefrontConfigResponse {
    private List<StorefrontNavCategory> navigation;
    private List<StorefrontCuratedCollection> curatedCollections;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StorefrontNavCategory {
        private String id;
        private String name;
        private String slug;
        private String bannerMediaUrl;
        private List<StorefrontNavItemType> itemTypes;
        private List<StorefrontNavFeaturedCollection> featuredCollections;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StorefrontNavItemType {
        private String id;
        private String name;
        private String slug;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StorefrontNavFeaturedCollection {
        private String name;
        private String slug;
        private String accentColor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StorefrontCuratedCollection {
        private String id;
        private String name;
        private String slug;
        private String tagline;
        private String description;
        private String heroImageUrl;
        private String accentColor;
        private Boolean isFeatured;
    }
}
