package com.fashionpin.productservice.repository;

import com.fashionpin.productservice.entity.ItemType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemTypeRepository extends JpaRepository<ItemType, String> {
    Optional<ItemType> findByCategoryIdAndSlug(String categoryId, String slug);
    Optional<ItemType> findBySlug(String slug);
    List<ItemType> findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(String categoryId);
    boolean existsByCategoryIdAndSlug(String categoryId, String slug);
}
