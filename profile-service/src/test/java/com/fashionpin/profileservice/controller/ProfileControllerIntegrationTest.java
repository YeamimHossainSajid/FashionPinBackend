package com.fashionpin.profileservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fashionpin.profileservice.dto.UpdateProfileRequest;
import com.fashionpin.profileservice.entity.Profile;
import com.fashionpin.profileservice.repository.ProfileRepository;
import com.fashionpin.profileservice.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private Profile testProfile;
    private String jwtStr;

    @BeforeEach
    void setUp() {
        String userId = UUID.randomUUID().toString();
        testProfile = Profile.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .displayName("Stylist Profile")
                .username("stylist_99")
                .bio("Official bio")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        profileRepository.save(testProfile);

        jwtStr = jwtService.generateAccessToken(userId, "stylist@fashionpin.com", List.of("ROLE_USER"));
    }

    @Test
    void getProfileByUserId_Authenticated_Success() throws Exception {
        mockMvc.perform(get("/api/profiles/" + testProfile.getUserId())
                        .header("Authorization", "Bearer " + jwtStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(testProfile.getUserId()))
                .andExpect(jsonPath("$.data.username").value("stylist_99"));
    }

    @Test
    void getCurrentProfile_Authenticated_Success() throws Exception {
        mockMvc.perform(get("/api/profiles/me")
                        .header("Authorization", "Bearer " + jwtStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(testProfile.getUserId()));
    }

    @Test
    void updateCurrentProfile_Authenticated_Success() throws Exception {
        UpdateProfileRequest updateReq = UpdateProfileRequest.builder()
                .displayName("Updated Stylist")
                .bio("New bio description")
                .location("Milan, Italy")
                .build();

        mockMvc.perform(patch("/api/profiles/me")
                        .header("Authorization", "Bearer " + jwtStr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.displayName").value("Updated Stylist"))
                .andExpect(jsonPath("$.data.location").value("Milan, Italy"));
    }
}
