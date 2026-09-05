package com.fashionpin.productservice.repository;

import com.fashionpin.productservice.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Page<Product> findByCategory(String category, Pageable pageable);
    Page<Product> findByBrandId(String brandId, Pageable pageable);
    Page<Product> findByStatus(String status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.dynamicCategory.slug = :categorySlug AND p.status = 'ACTIVE'")
    Page<Product> findByDynamicCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.dynamicCategory.slug = :categorySlug AND p.dynamicItemType.slug = :itemTypeSlug AND p.status = 'ACTIVE'")
    Page<Product> findByDynamicCategorySlugAndItemTypeSlug(
            @Param("categorySlug") String categorySlug,
            @Param("itemTypeSlug") String itemTypeSlug,
            Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.collectionProducts cp WHERE cp.collection.slug = :collectionSlug AND p.status = 'ACTIVE' ORDER BY cp.displayOrder ASC")
    Page<Product> findByCollectionSlug(@Param("collectionSlug") String collectionSlug, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.collectionProducts cp WHERE cp.collection.id = :collectionId AND p.status = 'ACTIVE' ORDER BY cp.displayOrder ASC")
    List<Product> findByCollectionId(@Param("collectionId") String collectionId);
}
