package com.fashionpin.authservice.repository;

import com.fashionpin.authservice.entity.RefreshToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findByUserAccountIdAndRevokedFalse(String userAccountId);
}
