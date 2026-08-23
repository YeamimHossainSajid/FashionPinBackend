package com.fashionpin.brandintegrationservice.repository;

import com.fashionpin.brandintegrationservice.entity.Brand;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, String> {
    Optional<Brand> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
