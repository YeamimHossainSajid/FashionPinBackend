package com.fashionpin.productservice.repository;

import com.fashionpin.productservice.entity.CollectionProduct;
import com.fashionpin.productservice.entity.CollectionProduct.CollectionProductId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionProductRepository extends JpaRepository<CollectionProduct, CollectionProductId> {
    List<CollectionProduct> findByIdCollectionIdOrderByDisplayOrderAsc(String collectionId);
    List<CollectionProduct> findByIdProductId(String productId);
    void deleteByIdCollectionIdAndIdProductId(String collectionId, String productId);
}
