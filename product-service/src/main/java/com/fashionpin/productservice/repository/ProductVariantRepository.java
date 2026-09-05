package com.fashionpin.productservice.repository;

import com.fashionpin.productservice.entity.ProductVariant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    List<ProductVariant> findByProductIdAndIsActiveTrue(String productId);
    List<ProductVariant> findByProductId(String productId);
    Optional<ProductVariant> findBySku(String sku);
    boolean existsBySku(String sku);
}
