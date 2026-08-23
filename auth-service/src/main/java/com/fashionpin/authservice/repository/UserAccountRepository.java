package com.fashionpin.authservice.repository;

import com.fashionpin.authservice.entity.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    Optional<UserAccount> findByEmail(String email);
    boolean existsByEmail(String email);
}
