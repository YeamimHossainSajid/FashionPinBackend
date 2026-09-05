package com.fashionpin.shoppingservice.repository;

import com.fashionpin.shoppingservice.entity.ShoppingCart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, String> {
    Optional<ShoppingCart> findByUserId(String userId);
    Optional<ShoppingCart> findByGuestSessionToken(String guestSessionToken);
}
