package com.fashionpin.shoppingservice.repository;

import com.fashionpin.shoppingservice.entity.PromoCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, String> {
    Optional<PromoCode> findByCodeIgnoreCaseAndIsActiveTrue(String code);
}
