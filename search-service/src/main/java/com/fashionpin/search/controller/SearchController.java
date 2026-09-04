package com.fashionpin.search.controller;

import com.fashionpin.search.dto.ProductSearchQueryCriteria;
import com.fashionpin.search.dto.ProductSearchResponse;
import com.fashionpin.search.dto.SearchSortBy;
import com.fashionpin.search.service.SearchCatalogService;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchCatalogService searchCatalogService;

    public SearchController(SearchCatalogService searchCatalogService) {
        this.searchCatalogService = searchCatalogService;
    }

    @GetMapping("/products")
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "RELEVANCE") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        validatePaginationAndBounds(minPrice, maxPrice, page, pageSize, sortBy);

        ProductSearchQueryCriteria criteria = ProductSearchQueryCriteria.builder()
                .query(query)
                .brandId(brandId)
                .category(category)
                .subcategory(subcategory)
                .color(color)
                .size(size)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .inStock(inStock)
                .sortBy(sortBy)
                .page(page)
                .pageSize(pageSize)
                .build();

        ProductSearchResponse response = searchCatalogService.searchProducts(criteria);
        return ResponseEntity.ok(response);
    }

    private void validatePaginationAndBounds(
            BigDecimal minPrice, BigDecimal maxPrice, int page, int pageSize, String sortBy) {
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("minPrice must not be negative");
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("maxPrice must not be negative");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }
        SearchSortBy.fromString(sortBy);
    }
}
