package com.fashionpin.productservice.repository;

import com.fashionpin.productservice.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<Category> findByParentIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
    boolean existsBySlug(String slug);
}
