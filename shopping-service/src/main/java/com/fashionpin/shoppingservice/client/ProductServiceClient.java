package com.fashionpin.shoppingservice.client;

import com.fashionpin.common.dto.ApiResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/api/v1/health")
    Map<String, Object> health();

    @GetMapping("/api/products/{id}")
    ApiResponse<ProductDto> getProductById(@PathVariable("id") String id);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ProductDto {
        private String id;
        private String name;
        private String slug;
        private BigDecimal price;
        private String currency;
        private String primaryMediaId;
        private String color;
        private String size;
        private List<ProductVariantDto> variants;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ProductVariantDto {
        private String id;
        private String productId;
        private String sku;
        private String colorName;
        private String colorHex;
        private String size;
        private BigDecimal priceOverride;
        private BigDecimal effectivePrice;
        private Integer stockQuantity;
        private String mediaIds;
    }
}
