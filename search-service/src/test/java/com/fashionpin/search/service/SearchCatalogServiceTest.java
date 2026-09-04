package com.fashionpin.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fashionpin.search.domain.model.FashionPostSearchIndex;
import com.fashionpin.search.domain.model.ProductSearchIndex;
import com.fashionpin.search.dto.FashionPostSearchQueryCriteria;
import com.fashionpin.search.dto.FashionPostSearchResponse;
import com.fashionpin.search.dto.ProductSearchQueryCriteria;
import com.fashionpin.search.dto.ProductSearchResponse;
import com.fashionpin.search.dto.SearchFacetResultDto;
import com.fashionpin.search.dto.SearchSuggestionResponse;
import com.fashionpin.search.repository.FashionPostSearchIndexRepository;
import com.fashionpin.search.repository.ProductSearchIndexRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SearchCatalogServiceTest {

    @Mock
    private ProductSearchIndexRepository productRepository;

    @Mock
    private FashionPostSearchIndexRepository postRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SearchCatalogService searchCatalogService;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @BeforeEach
    void setUp() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
    }

    @Test
    @DisplayName("searchProducts should return paginated response with mapped items and facets")
    void searchProducts_shouldReturnPaginatedResponseWithFacets() {
        UUID productId = UUID.randomUUID();
        UUID brandId = UUID.randomUUID();
        ProductSearchIndex product = ProductSearchIndex.builder()
                .id(productId)
                .brandId(brandId)
                .title("Silk Dress")
                .description("Elegant evening dress")
                .category("Dresses")
                .subcategory("Evening")
                .colors(List.of("Red", "Black"))
                .sizes(List.of("S", "M"))
                .priceAmount(new BigDecimal("149.99"))
                .priceCurrency("USD")
                .tags(List.of("silk", "evening"))
                .inStock(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Page<ProductSearchIndex> page = new PageImpl<>(List.of(product), Pageable.ofSize(20), 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // Setup facet queries
        CriteriaQuery<Object[]> objectQuery = mock(CriteriaQuery.class);
        Root<ProductSearchIndex> root = mock(Root.class);
        when(criteriaBuilder.createQuery(Object[].class)).thenReturn(objectQuery);
        when(objectQuery.from(ProductSearchIndex.class)).thenReturn(root);
        when(root.get(anyString())).thenReturn(mock(Path.class));

        TypedQuery<Object[]> brandTypedQuery = mock(TypedQuery.class);
        when(brandTypedQuery.getResultList()).thenReturn(List.of(new Object[]{brandId, 1L}));

        TypedQuery<Object[]> catTypedQuery = mock(TypedQuery.class);
        when(catTypedQuery.getResultList()).thenReturn(List.of(new Object[]{"Dresses", 1L}));

        TypedQuery<Object[]> priceTypedQuery = mock(TypedQuery.class);
        when(priceTypedQuery.getResultList()).thenReturn(List.of(new Object[]{new BigDecimal("149.99"), new BigDecimal("149.99")}));

        CriteriaQuery<Tuple> tupleQuery = mock(CriteriaQuery.class);
        Root<ProductSearchIndex> tupleRoot = mock(Root.class);
        when(criteriaBuilder.createTupleQuery()).thenReturn(tupleQuery);
        when(tupleQuery.from(ProductSearchIndex.class)).thenReturn(tupleRoot);
        when(tupleRoot.get(anyString())).thenReturn(mock(Path.class));

        Tuple tuple = mock(Tuple.class);
        when(tuple.get("colors")).thenReturn(List.of("Red", "Black"));
        when(tuple.get("sizes")).thenReturn(List.of("S", "M"));

        TypedQuery<Tuple> tupleTypedQuery = mock(TypedQuery.class);
        when(tupleTypedQuery.setMaxResults(any(Integer.class))).thenReturn(tupleTypedQuery);
        when(tupleTypedQuery.getResultList()).thenReturn(List.of(tuple));

        when(entityManager.createQuery(objectQuery))
                .thenReturn(brandTypedQuery)
                .thenReturn(catTypedQuery)
                .thenReturn(priceTypedQuery);
        when(entityManager.createQuery(tupleQuery)).thenReturn(tupleTypedQuery);

        ProductSearchQueryCriteria criteria = ProductSearchQueryCriteria.builder()
                .query("silk")
                .page(0)
                .pageSize(20)
                .sortBy("RELEVANCE")
                .build();

        ProductSearchResponse response = searchCatalogService.searchProducts(criteria);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals("Silk Dress", response.getItems().get(0).getTitle());
        assertEquals(0, response.getPage());
        assertEquals(20, response.getPageSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());

        assertNotNull(response.getFacets());
        assertEquals(1L, response.getFacets().getBrands().get(brandId.toString()));
        assertEquals(1L, response.getFacets().getCategories().get("Dresses"));
        assertEquals(1L, response.getFacets().getColors().get("Red"));
        assertEquals(1L, response.getFacets().getSizes().get("S"));
        assertEquals(new BigDecimal("149.99"), response.getFacets().getMinPrice());
    }

    @Test
    @DisplayName("searchPosts should return paginated response with mapped items")
    void searchPosts_shouldReturnPaginatedResponse() {
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        FashionPostSearchIndex post = FashionPostSearchIndex.builder()
                .id(postId)
                .authorUserId(authorId)
                .caption("Sunny day fit")
                .style("Casual")
                .occasion("Daily")
                .tags(List.of("summer", "casual"))
                .mediaIds(List.of(UUID.randomUUID()))
                .createdAt(Instant.now())
                .build();

        Page<FashionPostSearchIndex> page = new PageImpl<>(List.of(post), Pageable.ofSize(10), 1);
        when(postRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        FashionPostSearchQueryCriteria criteria = FashionPostSearchQueryCriteria.builder()
                .query("fit")
                .page(0)
                .pageSize(10)
                .build();

        FashionPostSearchResponse response = searchCatalogService.searchPosts(criteria);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals("Sunny day fit", response.getItems().get(0).getCaption());
        assertEquals(authorId, response.getItems().get(0).getAuthorUserId());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getPageSize());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("getSuggestions should return empty when query prefix is less than 2 characters")
    void getSuggestions_whenPrefixShort_shouldReturnEmptyImmediately() {
        SearchSuggestionResponse responseShort = searchCatalogService.getSuggestions("a", 10);
        assertNotNull(responseShort);
        assertTrue(responseShort.getSuggestions().isEmpty());
        assertTrue(responseShort.getCategories().isEmpty());
        assertTrue(responseShort.getTags().isEmpty());

        SearchSuggestionResponse responseNull = searchCatalogService.getSuggestions(null, 10);
        assertTrue(responseNull.getSuggestions().isEmpty());

        verify(productRepository, never()).findDistinctTitlesByPrefix(any(), any());
    }

    @Test
    @DisplayName("getSuggestions should query titles, categories, and tags when prefix is valid")
    void getSuggestions_whenPrefixValid_shouldReturnSuggestions() {
        when(productRepository.findDistinctTitlesByPrefix(any(), any()))
                .thenReturn(List.of("Silk Shirt", "Silk Skirt"));
        when(productRepository.findDistinctCategoriesByPrefix(any(), any()))
                .thenReturn(List.of("Shirts"));
        when(productRepository.findAllProductTags(any()))
                .thenReturn(List.of(List.of("silk", "summer", "vintage")));

        SearchSuggestionResponse response = searchCatalogService.getSuggestions("sil", 10);

        assertNotNull(response);
        assertEquals(2, response.getSuggestions().size());
        assertEquals("Silk Shirt", response.getSuggestions().get(0));
        assertEquals(1, response.getCategories().size());
        assertEquals("Shirts", response.getCategories().get(0));
        assertEquals(1, response.getTags().size());
        assertEquals("silk", response.getTags().get(0));
    }

    private String anyString() {
        return org.mockito.ArgumentMatchers.anyString();
    }
}
