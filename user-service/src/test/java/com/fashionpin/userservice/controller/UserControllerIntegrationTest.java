package com.fashionpin.userservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fashionpin.userservice.dto.UpdateUserRequest;
import com.fashionpin.userservice.entity.User;
import com.fashionpin.userservice.repository.UserRepository;
import com.fashionpin.userservice.security.JwtService;
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
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String jwtStr;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID().toString())
                .email("testuser@fashionpin.com")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userRepository.save(testUser);

        jwtStr = jwtService.generateAccessToken(testUser.getId(), testUser.getEmail(), List.of("ROLE_USER"));
    }

    @Test
    void getCurrentUser_Unauthenticated_Returns403() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCurrentUser_Authenticated_Success() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + jwtStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testUser.getId()))
                .andExpect(jsonPath("$.data.email").value("testuser@fashionpin.com"));
    }

    @Test
    void updateCurrentUser_Authenticated_Success() throws Exception {
        UpdateUserRequest updateReq = UpdateUserRequest.builder()
                .email("updateduser@fashionpin.com")
                .build();

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + jwtStr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("updateduser@fashionpin.com"));
    }

    @Test
    void deleteCurrentUser_Authenticated_Success() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + jwtStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
