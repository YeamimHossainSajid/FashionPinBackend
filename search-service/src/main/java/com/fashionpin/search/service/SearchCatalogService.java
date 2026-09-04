package com.fashionpin.search.service;

import com.fashionpin.search.domain.model.ProductSearchIndex;
import com.fashionpin.search.dto.ProductSearchIndexDto;
import com.fashionpin.search.dto.ProductSearchQueryCriteria;
import com.fashionpin.search.dto.ProductSearchResponse;
import com.fashionpin.search.dto.SearchFacetResultDto;
import com.fashionpin.search.repository.FashionPostSearchIndexRepository;
import com.fashionpin.search.repository.ProductSearchIndexRepository;
import com.fashionpin.search.service.specification.ProductSearchSpecifications;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchCatalogService {

    private final ProductSearchIndexRepository productRepository;
    private final FashionPostSearchIndexRepository postRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public SearchCatalogService(
            ProductSearchIndexRepository productRepository,
            FashionPostSearchIndexRepository postRepository,
            EntityManager entityManager) {
        this.productRepository = productRepository;
        this.postRepository = postRepository;
        this.entityManager = entityManager;
    }

    public ProductSearchResponse searchProducts(ProductSearchQueryCriteria criteria) {
        if (criteria == null) {
            criteria = new ProductSearchQueryCriteria();
        }

        int page = Math.max(0, criteria.getPage());
        int pageSize = criteria.getPageSize() <= 0 ? 20 : Math.min(criteria.getPageSize(), 100);

        Specification<ProductSearchIndex> spec = ProductSearchSpecifications.build(criteria);
        Sort sort = ProductSearchSpecifications.resolveSort(criteria.getSortBy());
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<ProductSearchIndex> pageResult = productRepository.findAll(spec, pageable);
        List<ProductSearchIndexDto> items = pageResult.getContent().stream()
                .map(ProductSearchIndexDto::fromEntity)
                .toList();

        SearchFacetResultDto facets = computeFacets(criteria);

        return ProductSearchResponse.builder()
                .items(items)
                .page(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .facets(facets)
                .build();
    }

    public SearchFacetResultDto computeFacets(ProductSearchQueryCriteria baseFilter) {
        return SearchFacetResultDto.builder().build();
    }
}
