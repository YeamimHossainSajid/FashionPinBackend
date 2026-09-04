package com.fashionpin.search.repository;

import com.fashionpin.search.domain.model.ProductSearchIndex;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSearchIndexRepository
        extends JpaRepository<ProductSearchIndex, UUID>, JpaSpecificationExecutor<ProductSearchIndex> {

    @Query("SELECT DISTINCT p.title FROM ProductSearchIndex p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :prefix, '%')) ORDER BY p.title ASC")
    List<String> findDistinctTitlesByPrefix(@Param("prefix") String prefix, Pageable pageable);

    @Query("SELECT DISTINCT p.category FROM ProductSearchIndex p WHERE LOWER(p.category) LIKE LOWER(CONCAT('%', :prefix, '%')) ORDER BY p.category ASC")
    List<String> findDistinctCategoriesByPrefix(@Param("prefix") String prefix, Pageable pageable);

    @Query("SELECT p.tags FROM ProductSearchIndex p WHERE p.tags IS NOT NULL")
    List<List<String>> findAllProductTags(Pageable pageable);
}
