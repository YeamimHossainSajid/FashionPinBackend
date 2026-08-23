package com.fashionpin.profileservice.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    private String profileImage;
    private String gender;
    private String dateOfBirth;
    private String location;
}
