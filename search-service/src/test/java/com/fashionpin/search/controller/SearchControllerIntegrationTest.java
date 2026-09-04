package com.fashionpin.search.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fashionpin.search.dto.FashionPostSearchIndexDto;
import com.fashionpin.search.dto.FashionPostSearchResponse;
import com.fashionpin.search.dto.ProductSearchIndexDto;
import com.fashionpin.search.dto.ProductSearchResponse;
import com.fashionpin.search.dto.SearchFacetResultDto;
import com.fashionpin.search.dto.SearchSuggestionResponse;
import com.fashionpin.search.service.SearchCatalogService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchCatalogService searchCatalogService;

    @Test
    @DisplayName("GET /api/search/products with valid params should return 200 and product response")
    void searchProducts_validRequest_shouldReturn200AndPayload() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID brandId = UUID.randomUUID();

        ProductSearchIndexDto item = ProductSearchIndexDto.builder()
                .id(productId)
                .brandId(brandId)
                .title("Silk Floral Dress")
                .category("Dresses")
                .priceAmount(new BigDecimal("129.99"))
                .priceCurrency("USD")
                .inStock(true)
                .build();

        SearchFacetResultDto facets = SearchFacetResultDto.builder()
                .brands(Map.of(brandId.toString(), 1L))
                .categories(Map.of("Dresses", 1L))
                .colors(Map.of("Floral", 1L))
                .minPrice(new BigDecimal("129.99"))
                .maxPrice(new BigDecimal("129.99"))
                .build();

        ProductSearchResponse response = ProductSearchResponse.builder()
                .items(List.of(item))
                .page(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .facets(facets)
                .build();

        given(searchCatalogService.searchProducts(any())).willReturn(response);

        mockMvc.perform(get("/api/search/products")
                        .param("q", "floral")
                        .param("category", "Dresses")
                        .param("minPrice", "50")
                        .param("maxPrice", "200")
                        .param("sortBy", "PRICE_ASC")
                        .param("page", "0")
                        .param("pageSize", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(productId.toString()))
                .andExpect(jsonPath("$.items[0].title").value("Silk Floral Dress"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.facets.categories.Dresses").value(1));
    }

    @Test
    @DisplayName("GET /api/search/products with minPrice > maxPrice should return 400")
    void searchProducts_minPriceGreaterThanMaxPrice_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/search/products")
                        .param("minPrice", "200")
                        .param("maxPrice", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"));
    }

    @Test
    @DisplayName("GET /api/search/products with invalid sort key should return 400")
    void searchProducts_invalidSortKey_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/search/products")
                        .param("sortBy", "INVALID_SORT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"));
    }

    @Test
    @DisplayName("GET /api/search/products with malformed UUID should return 400")
    void searchProducts_malformedUUID_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/search/products")
                        .param("brandId", "not-a-valid-uuid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("TYPE_MISMATCH"));
    }

    @Test
    @DisplayName("GET /api/search/posts with valid params should return 200")
    void searchPosts_validRequest_shouldReturn200() throws Exception {
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        FashionPostSearchIndexDto postItem = FashionPostSearchIndexDto.builder()
                .id(postId)
                .authorUserId(authorId)
                .caption("Summer Street Style")
                .style("Streetwear")
                .occasion("Casual")
                .tags(List.of("summer", "streetwear"))
                .build();

        FashionPostSearchResponse response = FashionPostSearchResponse.builder()
                .items(List.of(postItem))
                .page(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        given(searchCatalogService.searchPosts(any())).willReturn(response);

        mockMvc.perform(get("/api/search/posts")
                        .param("q", "street")
                        .param("style", "Streetwear")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(postId.toString()))
                .andExpect(jsonPath("$.items[0].caption").value("Summer Street Style"));
    }

    @Test
    @DisplayName("GET /api/search/suggestions with valid query prefix should return 200")
    void getSuggestions_validPrefix_shouldReturn200() throws Exception {
        SearchSuggestionResponse response = SearchSuggestionResponse.builder()
                .suggestions(List.of("Leather Jacket"))
                .categories(List.of("Jackets"))
                .tags(List.of("leather"))
                .build();

        given(searchCatalogService.getSuggestions("lea", 10)).willReturn(response);

        mockMvc.perform(get("/api/search/suggestions")
                        .param("q", "lea")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions[0]").value("Leather Jacket"))
                .andExpect(jsonPath("$.categories[0]").value("Jackets"));
    }

    @Test
    @DisplayName("GET /api/search/suggestions with query less than 2 characters should return 400")
    void getSuggestions_shortPrefix_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/search/suggestions")
                        .param("q", "a")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"));
    }

    @Test
    @DisplayName("GET /api/search/facets with valid params should return 200")
    void getFacets_validRequest_shouldReturn200() throws Exception {
        SearchFacetResultDto facets = SearchFacetResultDto.builder()
                .brands(Map.of(UUID.randomUUID().toString(), 3L))
                .categories(Map.of("Dresses", 5L))
                .minPrice(new BigDecimal("19.99"))
                .maxPrice(new BigDecimal("299.99"))
                .build();

        given(searchCatalogService.computeFacets(any())).willReturn(facets);

        mockMvc.perform(get("/api/search/facets")
                        .param("category", "Dresses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories.Dresses").value(5))
                .andExpect(jsonPath("$.minPrice").value(19.99))
                .andExpect(jsonPath("$.maxPrice").value(299.99));
    }
}
