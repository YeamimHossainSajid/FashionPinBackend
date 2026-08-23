package com.fashionpin.profileservice.repository;

import com.fashionpin.profileservice.entity.Profile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findByUserId(String userId);
    Optional<Profile> findByUsername(String username);
    boolean existsByUserId(String userId);
    boolean existsByUsername(String username);
}
