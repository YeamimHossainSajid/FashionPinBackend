package com.fashionpin.productservice.repository;

import com.fashionpin.productservice.entity.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, String> {
    Optional<Collection> findBySlug(String slug);
    List<Collection> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<Collection> findByIsFeaturedTrueAndIsActiveTrueOrderByDisplayOrderAsc();
    boolean existsBySlug(String slug);
}
