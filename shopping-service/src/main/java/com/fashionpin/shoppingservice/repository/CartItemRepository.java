package com.fashionpin.shoppingservice.repository;

import com.fashionpin.shoppingservice.entity.CartItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    Optional<CartItem> findByCartIdAndVariantId(String cartId, String variantId);
    void deleteByCartId(String cartId);
}
