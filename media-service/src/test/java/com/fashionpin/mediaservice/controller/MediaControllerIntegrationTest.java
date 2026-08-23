package com.fashionpin.mediaservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fashionpin.mediaservice.entity.Media;
import com.fashionpin.mediaservice.repository.MediaRepository;
import com.fashionpin.mediaservice.security.JwtService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MediaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private JwtService jwtService;

    private String jwtStr;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        jwtStr = jwtService.generateAccessToken(userId, "uploader@fashionpin.com", List.of("ROLE_USER"));
    }

    @Test
    void uploadMedia_Authenticated_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake image bytes".getBytes());

        mockMvc.perform(multipart("/api/media/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + jwtStr))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mediaType").value("IMAGE"))
                .andExpect(jsonPath("$.data.ownerId").value(userId));
    }

    @Test
    void getMediaMetadata_Authenticated_Success() throws Exception {
        Media media = Media.create(userId, "IMAGE", "key-123", "outfit.jpg", "image/jpeg", 2048);
        mediaRepository.save(media);

        mockMvc.perform(get("/api/media/" + media.getId() + "/metadata")
                        .header("Authorization", "Bearer " + jwtStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(media.getId()))
                .andExpect(jsonPath("$.data.originalFilename").value("outfit.jpg"));
    }

    @Test
    void deleteMedia_Authenticated_Success() throws Exception {
        Media media = Media.create(userId, "IMAGE", "key-delete", "outfit.jpg", "image/jpeg", 2048);
        mediaRepository.save(media);

        mockMvc.perform(delete("/api/media/" + media.getId())
                        .header("Authorization", "Bearer " + jwtStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
