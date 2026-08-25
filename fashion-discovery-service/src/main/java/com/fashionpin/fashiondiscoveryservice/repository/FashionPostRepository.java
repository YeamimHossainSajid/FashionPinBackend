package com.fashionpin.fashiondiscoveryservice.repository;

import com.fashionpin.fashiondiscoveryservice.entity.FashionPost;
import com.fashionpin.fashiondiscoveryservice.entity.Occasion;
import com.fashionpin.fashiondiscoveryservice.entity.Style;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FashionPostRepository extends JpaRepository<FashionPost, String>, JpaSpecificationExecutor<FashionPost> {

    Page<FashionPost> findByAuthorUserId(String authorUserId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM FashionPost p LEFT JOIN p.tags t " +
           "WHERE (:style IS NULL OR p.style = :style) " +
           "AND (:occasion IS NULL OR p.occasion = :occasion) " +
           "AND (:color IS NULL OR (t.tagType = 'COLOR' AND LOWER(t.name) = LOWER(:color))) " +
           "AND (:season IS NULL OR (t.tagType = 'SEASON' AND LOWER(t.name) = LOWER(:season)))")
    Page<FashionPost> findDiscoveryPosts(
            @Param("style") Style style,
            @Param("occasion") Occasion occasion,
            @Param("color") String color,
            @Param("season") String season,
            Pageable pageable
    );
}
