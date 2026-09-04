package com.fashionpin.search.service.specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fashionpin.search.domain.model.FashionPostSearchIndex;
import com.fashionpin.search.domain.model.ProductSearchIndex;
import com.fashionpin.search.dto.FashionPostSearchQueryCriteria;
import com.fashionpin.search.dto.ProductSearchQueryCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class SearchSpecificationsTest {

    @Mock
    private Root<ProductSearchIndex> productRoot;

    @Mock
    private Root<FashionPostSearchIndex> postRoot;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Predicate dummyPredicate;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should build composite predicates for combined price, category, and text filter")
    void buildProductSpec_withCombinedFilters_shouldCreatePredicates() {
        ProductSearchQueryCriteria criteria = ProductSearchQueryCriteria.builder()
                .query("silk dress")
                .category("Dresses")
                .subcategory("Evening")
                .minPrice(new BigDecimal("50.00"))
                .maxPrice(new BigDecimal("200.00"))
                .color("Red")
                .size("M")
                .inStock(true)
                .brandId(UUID.randomUUID())
                .build();

        Path<Object> titlePath = mock(Path.class);
        Path<Object> descPath = mock(Path.class);
        Path<Object> tagsPath = mock(Path.class);
        Path<Object> catPath = mock(Path.class);
        Path<Object> subcatPath = mock(Path.class);
        Path<Object> pricePath = mock(Path.class);
        Path<Object> colorsPath = mock(Path.class);
        Path<Object> sizesPath = mock(Path.class);
        Path<Object> inStockPath = mock(Path.class);
        Path<Object> brandIdPath = mock(Path.class);

        when(productRoot.get("title")).thenReturn(titlePath);
        when(productRoot.get("description")).thenReturn(descPath);
        when(productRoot.get("tags")).thenReturn(tagsPath);
        when(productRoot.get("category")).thenReturn(catPath);
        when(productRoot.get("subcategory")).thenReturn(subcatPath);
        when(productRoot.get("priceAmount")).thenReturn(pricePath);
        when(productRoot.get("colors")).thenReturn(colorsPath);
        when(productRoot.get("sizes")).thenReturn(sizesPath);
        when(productRoot.get("inStock")).thenReturn(inStockPath);
        when(productRoot.get("brandId")).thenReturn(brandIdPath);

        when(cb.lower(any())).thenReturn(mock(Expression.class));
        when(cb.like(any(), anyString())).thenReturn(dummyPredicate);
        when(cb.or(any(Predicate[].class))).thenReturn(dummyPredicate);
        when(cb.equal(any(), any())).thenReturn(dummyPredicate);
        when(cb.greaterThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(dummyPredicate);
        when(cb.lessThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(dummyPredicate);
        when(cb.and(any(Predicate[].class))).thenReturn(dummyPredicate);

        Specification<ProductSearchIndex> spec = ProductSearchSpecifications.build(criteria);
        Predicate result = spec.toPredicate(productRoot, query, cb);

        assertNotNull(result);
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should return conjunction when product criteria is empty")
    void buildProductSpec_whenEmptyCriteria_shouldReturnConjunction() {
        when(cb.conjunction()).thenReturn(dummyPredicate);

        Specification<ProductSearchIndex> spec = ProductSearchSpecifications.build(new ProductSearchQueryCriteria());
        Predicate result = spec.toPredicate(productRoot, query, cb);

        assertNotNull(result);
        verify(cb).conjunction();
    }

    @Test
    @DisplayName("Should correctly resolve sort orders")
    void resolveSort_shouldReturnExpectedSortDirectionAndProperty() {
        Sort priceAsc = ProductSearchSpecifications.resolveSort("PRICE_ASC");
        assertEquals(Sort.Direction.ASC, priceAsc.getOrderFor("priceAmount").getDirection());

        Sort priceDesc = ProductSearchSpecifications.resolveSort("PRICE_DESC");
        assertEquals(Sort.Direction.DESC, priceDesc.getOrderFor("priceAmount").getDirection());

        Sort newest = ProductSearchSpecifications.resolveSort("NEWEST");
        assertEquals(Sort.Direction.DESC, newest.getOrderFor("createdAt").getDirection());

        Sort relevance = ProductSearchSpecifications.resolveSort("RELEVANCE");
        assertEquals(Sort.Direction.DESC, relevance.getOrderFor("updatedAt").getDirection());

        Sort defaultSort = ProductSearchSpecifications.resolveSort(null);
        assertEquals(Sort.Direction.DESC, defaultSort.getOrderFor("updatedAt").getDirection());

        assertThrows(IllegalArgumentException.class, () -> ProductSearchSpecifications.resolveSort("UNKNOWN_SORT"));
    }

    @Test
    @DisplayName("Should build composite predicates for fashion post criteria")
    void buildPostSpec_withFilters_shouldCreatePredicates() {
        UUID authorId = UUID.randomUUID();
        FashionPostSearchQueryCriteria criteria = FashionPostSearchQueryCriteria.builder()
                .query("summer vibes")
                .style("Casual")
                .occasion("Beach")
                .authorUserId(authorId)
                .tag("summer")
                .build();

        Path<Object> captionPath = mock(Path.class);
        Path<Object> tagsPath = mock(Path.class);
        Path<Object> stylePath = mock(Path.class);
        Path<Object> occasionPath = mock(Path.class);
        Path<Object> authorPath = mock(Path.class);

        when(postRoot.get("caption")).thenReturn(captionPath);
        when(postRoot.get("tags")).thenReturn(tagsPath);
        when(postRoot.get("style")).thenReturn(stylePath);
        when(postRoot.get("occasion")).thenReturn(occasionPath);
        when(postRoot.get("authorUserId")).thenReturn(authorPath);

        when(cb.lower(any())).thenReturn(mock(Expression.class));
        when(cb.like(any(), anyString())).thenReturn(dummyPredicate);
        when(cb.or(any(Predicate[].class))).thenReturn(dummyPredicate);
        when(cb.equal(any(), any())).thenReturn(dummyPredicate);
        when(cb.and(any(Predicate[].class))).thenReturn(dummyPredicate);

        Specification<FashionPostSearchIndex> spec = FashionPostSearchSpecifications.build(criteria);
        Predicate result = spec.toPredicate(postRoot, query, cb);

        assertNotNull(result);
        verify(cb).and(any(Predicate[].class));

        Sort sort = FashionPostSearchSpecifications.resolveSort();
        assertEquals(Sort.Direction.DESC, sort.getOrderFor("createdAt").getDirection());
    }
}
