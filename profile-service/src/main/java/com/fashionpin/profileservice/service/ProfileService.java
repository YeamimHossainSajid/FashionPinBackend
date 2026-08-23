package com.fashionpin.profileservice.service;

import com.fashionpin.common.exception.BusinessException;
import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.profileservice.dto.ProfileResponse;
import com.fashionpin.profileservice.dto.UpdateProfileRequest;
import com.fashionpin.profileservice.entity.Profile;
import com.fashionpin.profileservice.repository.ProfileRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUserId(String userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for userId: " + userId));
        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getCurrentProfile(String currentUserId) {
        return getProfileByUserId(currentUserId);
    }

    @Transactional
    public ProfileResponse updateCurrentProfile(String currentUserId, UpdateProfileRequest request) {
        Profile profile = profileRepository.findByUserId(currentUserId)
                .orElseGet(() -> Profile.createInitial(currentUserId, null));

        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String newUsername = request.getUsername().trim().toLowerCase();
            if (!newUsername.equalsIgnoreCase(profile.getUsername()) && profileRepository.existsByUsername(newUsername)) {
                throw new BusinessException("USERNAME_ALREADY_TAKEN", "Username is already taken");
            }
            profile.setUsername(newUsername);
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getProfileImage() != null) {
            profile.setProfileImage(request.getProfileImage());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }

        profile.setUpdatedAt(Instant.now());
        Profile saved = profileRepository.save(profile);
        return mapToResponse(saved);
    }

    private ProfileResponse mapToResponse(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .displayName(profile.getDisplayName())
                .username(profile.getUsername())
                .bio(profile.getBio())
                .profileImage(profile.getProfileImage())
                .gender(profile.getGender())
                .dateOfBirth(profile.getDateOfBirth())
                .location(profile.getLocation())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
