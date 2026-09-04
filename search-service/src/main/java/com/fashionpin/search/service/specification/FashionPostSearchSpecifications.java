package com.fashionpin.search.service.specification;

import com.fashionpin.search.domain.model.FashionPostSearchIndex;
import com.fashionpin.search.dto.FashionPostSearchQueryCriteria;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class FashionPostSearchSpecifications {

    private FashionPostSearchSpecifications() {
    }

    public static Specification<FashionPostSearchIndex> build(FashionPostSearchQueryCriteria criteria) {
        return (root, query, cb) -> {
            if (criteria == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(criteria.getQuery())) {
                String pattern = "%" + criteria.getQuery().trim().toLowerCase() + "%";
                Predicate captionMatch = cb.like(cb.lower(root.get("caption")), pattern);
                Predicate tagsMatch = cb.like(cb.lower(root.get("tags").as(String.class)), pattern);
                predicates.add(cb.or(captionMatch, tagsMatch));
            }

            if (StringUtils.hasText(criteria.getStyle())) {
                predicates.add(cb.equal(cb.lower(root.get("style")), criteria.getStyle().trim().toLowerCase()));
            }

            if (StringUtils.hasText(criteria.getOccasion())) {
                predicates.add(cb.equal(cb.lower(root.get("occasion")), criteria.getOccasion().trim().toLowerCase()));
            }

            if (criteria.getAuthorUserId() != null) {
                predicates.add(cb.equal(root.get("authorUserId"), criteria.getAuthorUserId()));
            }

            if (StringUtils.hasText(criteria.getTag())) {
                String tagPattern = "%" + criteria.getTag().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("tags").as(String.class)), tagPattern));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Sort resolveSort() {
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }
}
