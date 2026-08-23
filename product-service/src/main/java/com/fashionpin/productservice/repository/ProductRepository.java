package com.fashionpin.productservice.repository;

import com.fashionpin.productservice.entity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Page<Product> findByCategory(String category, Pageable pageable);
    Page<Product> findByBrandId(String brandId, Pageable pageable);
    Page<Product> findByStatus(String status, Pageable pageable);
}
