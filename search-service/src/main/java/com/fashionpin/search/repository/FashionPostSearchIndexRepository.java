package com.fashionpin.search.repository;

import com.fashionpin.search.domain.model.FashionPostSearchIndex;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FashionPostSearchIndexRepository
        extends JpaRepository<FashionPostSearchIndex, UUID>, JpaSpecificationExecutor<FashionPostSearchIndex> {
}
