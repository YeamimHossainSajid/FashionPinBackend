package com.fashionpin.profileservice.controller;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.profileservice.dto.ProfileResponse;
import com.fashionpin.profileservice.dto.UpdateProfileRequest;
import com.fashionpin.profileservice.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Tag(name = "Profiles", description = "User profile management endpoints")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping({"/api/profiles/{userId}", "/api/v1/profile/{userId}"})
    @Operation(summary = "Get user profile by userId")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfileByUserId(@PathVariable("userId") String userId) {
        ProfileResponse response = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping({"/api/profiles/me", "/api/v1/profile/me"})
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCurrentProfile(Principal principal) {
        String currentUserId = principal.getName();
        ProfileResponse response = profileService.getCurrentProfile(currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping({"/api/profiles/me", "/api/v1/profile/me"})
    @Operation(summary = "Update current authenticated user profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateCurrentProfile(
            Principal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        String currentUserId = principal.getName();
        ProfileResponse response = profileService.updateCurrentProfile(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", response));
    }
}
