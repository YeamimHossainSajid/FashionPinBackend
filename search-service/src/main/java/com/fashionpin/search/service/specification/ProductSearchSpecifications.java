package com.fashionpin.search.service.specification;

import com.fashionpin.search.domain.model.ProductSearchIndex;
import com.fashionpin.search.dto.ProductSearchQueryCriteria;
import com.fashionpin.search.dto.SearchSortBy;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ProductSearchSpecifications {

    private ProductSearchSpecifications() {
    }

    public static Specification<ProductSearchIndex> build(ProductSearchQueryCriteria criteria) {
        return (root, query, cb) -> {
            if (criteria == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(criteria.getQuery())) {
                String pattern = "%" + criteria.getQuery().trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                Predicate tagsMatch = cb.like(cb.lower(root.get("tags").as(String.class)), pattern);
                predicates.add(cb.or(titleMatch, descMatch, tagsMatch));
            }

            if (criteria.getBrandId() != null) {
                predicates.add(cb.equal(root.get("brandId"), criteria.getBrandId()));
            }

            if (StringUtils.hasText(criteria.getCategory())) {
                predicates.add(cb.equal(cb.lower(root.get("category")), criteria.getCategory().trim().toLowerCase()));
            }

            if (StringUtils.hasText(criteria.getSubcategory())) {
                predicates.add(cb.equal(cb.lower(root.get("subcategory")), criteria.getSubcategory().trim().toLowerCase()));
            }

            if (criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("priceAmount"), criteria.getMinPrice()));
            }

            if (criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("priceAmount"), criteria.getMaxPrice()));
            }

            if (StringUtils.hasText(criteria.getColor())) {
                String colorPattern = "%" + criteria.getColor().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("colors").as(String.class)), colorPattern));
            }

            if (StringUtils.hasText(criteria.getSize())) {
                String sizePattern = "%" + criteria.getSize().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("sizes").as(String.class)), sizePattern));
            }

            if (criteria.getInStock() != null) {
                predicates.add(cb.equal(root.get("inStock"), criteria.getInStock()));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Sort resolveSort(String sortBy) {
        SearchSortBy sortEnum = SearchSortBy.fromString(sortBy);
        return switch (sortEnum) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "priceAmount");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "priceAmount");
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case RELEVANCE -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };
    }
}
