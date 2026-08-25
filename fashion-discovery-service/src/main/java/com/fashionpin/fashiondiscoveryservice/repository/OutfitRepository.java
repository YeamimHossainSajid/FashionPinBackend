package com.fashionpin.fashiondiscoveryservice.repository;

import com.fashionpin.fashiondiscoveryservice.entity.Outfit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutfitRepository extends JpaRepository<Outfit, String> {
    Page<Outfit> findByAuthorUserId(String authorUserId, Pageable pageable);
}
