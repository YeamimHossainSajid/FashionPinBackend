package com.fashionpin.profileservice.entity;

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
@Table(name = "profiles")
public class Profile {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "display_name")
    private String displayName;

    @Column(unique = true)
    private String username;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_image")
    private String profileImage;

    private String gender;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    private String location;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Profile createInitial(String userId, String email) {
        Instant now = Instant.now();
        String defaultUsername = email != null && email.contains("@")
                ? email.substring(0, email.indexOf('@')).replaceAll("[^a-zA-Z0-9_]", "") + "_" + UUID.randomUUID().toString().substring(0, 6)
                : "user_" + UUID.randomUUID().toString().substring(0, 8);

        return Profile.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .displayName(email != null && email.contains("@") ? email.substring(0, email.indexOf('@')) : "User")
                .username(defaultUsername)
                .bio("")
                .profileImage("")
                .gender("")
                .dateOfBirth("")
                .location("")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
