package com.fashionpin.profileservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fashionpin.common.exception.BusinessException;
import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.profileservice.dto.ProfileResponse;
import com.fashionpin.profileservice.dto.UpdateProfileRequest;
import com.fashionpin.profileservice.entity.Profile;
import com.fashionpin.profileservice.repository.ProfileRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileService profileService;

    private Profile sampleProfile;

    @BeforeEach
    void setUp() {
        sampleProfile = Profile.builder()
                .id(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .displayName("Fashionista")
                .username("fashion_lover")
                .bio("Fashion & style enthusiast")
                .profileImage("https://example.com/avatar.jpg")
                .gender("Female")
                .dateOfBirth("1998-05-15")
                .location("Paris, France")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void getProfileByUserId_Success() {
        when(profileRepository.findByUserId(sampleProfile.getUserId())).thenReturn(Optional.of(sampleProfile));

        ProfileResponse response = profileService.getProfileByUserId(sampleProfile.getUserId());

        assertNotNull(response);
        assertEquals(sampleProfile.getUserId(), response.getUserId());
        assertEquals("fashion_lover", response.getUsername());
    }

    @Test
    void getProfileByUserId_NotFound_ThrowsException() {
        when(profileRepository.findByUserId("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> profileService.getProfileByUserId("nonexistent"));
    }

    @Test
    void updateCurrentProfile_Success() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .displayName("New Name")
                .bio("Updated bio")
                .location("New York, USA")
                .build();

        when(profileRepository.findByUserId(sampleProfile.getUserId())).thenReturn(Optional.of(sampleProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> i.getArgument(0));

        ProfileResponse response = profileService.updateCurrentProfile(sampleProfile.getUserId(), request);

        assertNotNull(response);
        assertEquals("New Name", response.getDisplayName());
        assertEquals("Updated bio", response.getBio());
        assertEquals("New York, USA", response.getLocation());
    }

    @Test
    void updateCurrentProfile_DuplicateUsername_ThrowsException() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .username("taken_username")
                .build();

        when(profileRepository.findByUserId(sampleProfile.getUserId())).thenReturn(Optional.of(sampleProfile));
        when(profileRepository.existsByUsername("taken_username")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> profileService.updateCurrentProfile(sampleProfile.getUserId(), request));
        assertEquals("USERNAME_ALREADY_TAKEN", ex.getCode());
    }
}
