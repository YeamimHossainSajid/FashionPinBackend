package com.fashionpin.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "user_account_id", nullable = false)
    private String userAccountId;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
