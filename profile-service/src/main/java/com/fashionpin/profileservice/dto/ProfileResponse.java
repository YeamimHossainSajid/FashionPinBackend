package com.fashionpin.profileservice.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String id;
    private String userId;
    private String displayName;
    private String username;
    private String bio;
    private String profileImage;
    private String gender;
    private String dateOfBirth;
    private String location;
    private Instant createdAt;
    private Instant updatedAt;
}
