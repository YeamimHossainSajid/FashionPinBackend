package com.fashionpin.search.service;

import com.fashionpin.search.domain.model.FashionPostSearchIndex;
import com.fashionpin.search.domain.model.ProductSearchIndex;
import com.fashionpin.search.dto.FashionPostSearchIndexDto;
import com.fashionpin.search.dto.FashionPostSearchQueryCriteria;
import com.fashionpin.search.dto.FashionPostSearchResponse;
import com.fashionpin.search.dto.ProductSearchIndexDto;
import com.fashionpin.search.dto.ProductSearchQueryCriteria;
import com.fashionpin.search.dto.ProductSearchResponse;
import com.fashionpin.search.dto.SearchFacetResultDto;
import com.fashionpin.search.dto.SearchSuggestionResponse;
import com.fashionpin.search.repository.FashionPostSearchIndexRepository;
import com.fashionpin.search.repository.ProductSearchIndexRepository;
import com.fashionpin.search.service.specification.FashionPostSearchSpecifications;
import com.fashionpin.search.service.specification.ProductSearchSpecifications;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    public FashionPostSearchResponse searchPosts(FashionPostSearchQueryCriteria criteria) {
        if (criteria == null) {
            criteria = new FashionPostSearchQueryCriteria();
        }

        int page = Math.max(0, criteria.getPage());
        int pageSize = criteria.getPageSize() <= 0 ? 20 : Math.min(criteria.getPageSize(), 100);

        Specification<FashionPostSearchIndex> spec = FashionPostSearchSpecifications.build(criteria);
        Sort sort = FashionPostSearchSpecifications.resolveSort();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<FashionPostSearchIndex> pageResult = postRepository.findAll(spec, pageable);
        List<FashionPostSearchIndexDto> items = pageResult.getContent().stream()
                .map(FashionPostSearchIndexDto::fromEntity)
                .toList();

        return FashionPostSearchResponse.builder()
                .items(items)
                .page(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }

    public SearchFacetResultDto computeFacets(ProductSearchQueryCriteria baseFilter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Specification<ProductSearchIndex> spec = ProductSearchSpecifications.build(baseFilter);

        // 1. Brands with counts
        Map<String, Long> brands = new LinkedHashMap<>();
        CriteriaQuery<Object[]> brandCq = cb.createQuery(Object[].class);
        Root<ProductSearchIndex> brandRoot = brandCq.from(ProductSearchIndex.class);
        Predicate brandPred = spec.toPredicate(brandRoot, brandCq, cb);
        if (brandPred != null) {
            brandCq.where(brandPred);
        }
        brandCq.multiselect(brandRoot.get("brandId"), cb.count(brandRoot));
        brandCq.groupBy(brandRoot.get("brandId"));
        List<Object[]> brandRows = entityManager.createQuery(brandCq).getResultList();
        for (Object[] row : brandRows) {
            if (row[0] != null) {
                brands.put(row[0].toString(), (Long) row[1]);
            }
        }

        // 2. Categories with counts
        Map<String, Long> categories = new LinkedHashMap<>();
        CriteriaQuery<Object[]> catCq = cb.createQuery(Object[].class);
        Root<ProductSearchIndex> catRoot = catCq.from(ProductSearchIndex.class);
        Predicate catPred = spec.toPredicate(catRoot, catCq, cb);
        if (catPred != null) {
            catCq.where(catPred);
        }
        catCq.multiselect(catRoot.get("category"), cb.count(catRoot));
        catCq.groupBy(catRoot.get("category"));
        List<Object[]> catRows = entityManager.createQuery(catCq).getResultList();
        for (Object[] row : catRows) {
            if (row[0] != null) {
                categories.put((String) row[0], (Long) row[1]);
            }
        }

        // 3. Min/Max price bounds
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        CriteriaQuery<Object[]> priceCq = cb.createQuery(Object[].class);
        Root<ProductSearchIndex> priceRoot = priceCq.from(ProductSearchIndex.class);
        Predicate pricePred = spec.toPredicate(priceRoot, priceCq, cb);
        if (pricePred != null) {
            priceCq.where(pricePred);
        }
        priceCq.multiselect(cb.min(priceRoot.get("priceAmount")), cb.max(priceRoot.get("priceAmount")));
        List<Object[]> priceRows = entityManager.createQuery(priceCq).getResultList();
        if (!priceRows.isEmpty() && priceRows.get(0) != null) {
            Object[] p = priceRows.get(0);
            if (p[0] instanceof BigDecimal minVal) {
                minPrice = minVal;
            }
            if (p[1] instanceof BigDecimal maxVal) {
                maxPrice = maxVal;
            }
        }

        // 4. Colors and sizes distributions
        Map<String, Long> colors = new LinkedHashMap<>();
        Map<String, Long> sizes = new LinkedHashMap<>();
        CriteriaQuery<Tuple> colorSizeCq = cb.createTupleQuery();
        Root<ProductSearchIndex> colorSizeRoot = colorSizeCq.from(ProductSearchIndex.class);
        Predicate colorSizePred = spec.toPredicate(colorSizeRoot, colorSizeCq, cb);
        if (colorSizePred != null) {
            colorSizeCq.where(colorSizePred);
        }
        colorSizeCq.multiselect(colorSizeRoot.get("colors").alias("colors"), colorSizeRoot.get("sizes").alias("sizes"));
        List<Tuple> tuples = entityManager.createQuery(colorSizeCq).setMaxResults(1000).getResultList();
        for (Tuple tuple : tuples) {
            @SuppressWarnings("unchecked")
            List<String> colorList = (List<String>) tuple.get("colors");
            if (colorList != null) {
                for (String c : colorList) {
                    if (StringUtils.hasText(c)) {
                        colors.merge(c.trim(), 1L, Long::sum);
                    }
                }
            }
            @SuppressWarnings("unchecked")
            List<String> sizeList = (List<String>) tuple.get("sizes");
            if (sizeList != null) {
                for (String s : sizeList) {
                    if (StringUtils.hasText(s)) {
                        sizes.merge(s.trim(), 1L, Long::sum);
                    }
                }
            }
        }

        return SearchFacetResultDto.builder()
                .brands(brands)
                .categories(categories)
                .colors(colors)
                .sizes(sizes)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();
    }

    public SearchSuggestionResponse getSuggestions(String prefix, int limit) {
        if (!StringUtils.hasText(prefix) || prefix.trim().length() < 2) {
            return SearchSuggestionResponse.builder()
                    .suggestions(Collections.emptyList())
                    .categories(Collections.emptyList())
                    .tags(Collections.emptyList())
                    .build();
        }

        int effectiveLimit = limit <= 0 ? 10 : Math.min(limit, 50);
        Pageable pageable = PageRequest.of(0, effectiveLimit);
        String cleanPrefix = prefix.trim();

        List<String> titles = productRepository.findDistinctTitlesByPrefix(cleanPrefix, pageable);
        List<String> categories = productRepository.findDistinctCategoriesByPrefix(cleanPrefix, pageable);

        List<List<String>> tagLists = productRepository.findAllProductTags(PageRequest.of(0, 100));
        String lowerPrefix = cleanPrefix.toLowerCase();
        Set<String> matchedTags = new LinkedHashSet<>();
        if (tagLists != null) {
            for (List<String> tagList : tagLists) {
                if (tagList != null) {
                    for (String tag : tagList) {
                        if (tag != null && tag.toLowerCase().contains(lowerPrefix)) {
                            matchedTags.add(tag);
                            if (matchedTags.size() >= effectiveLimit) {
                                break;
                            }
                        }
                    }
                }
                if (matchedTags.size() >= effectiveLimit) {
                    break;
                }
            }
        }

        return SearchSuggestionResponse.builder()
                .suggestions(titles)
                .categories(categories)
                .tags(new ArrayList<>(matchedTags))
                .build();
    }
}
