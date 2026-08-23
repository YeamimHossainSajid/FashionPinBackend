package com.fashionpin.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String roles;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static UserAccount create(String email, String passwordHash) {
        Instant now = Instant.now();
        return UserAccount.builder()
                .id(UUID.randomUUID().toString())
                .email(email.toLowerCase().trim())
                .passwordHash(passwordHash)
                .status("ACTIVE")
                .roles("ROLE_USER")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
