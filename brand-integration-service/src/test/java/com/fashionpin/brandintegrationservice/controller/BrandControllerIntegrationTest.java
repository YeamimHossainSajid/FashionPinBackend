package com.fashionpin.brandintegrationservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fashionpin.brandintegrationservice.dto.CreateBrandRequest;
import com.fashionpin.brandintegrationservice.dto.UpdateBrandRequest;
import com.fashionpin.brandintegrationservice.entity.Brand;
import com.fashionpin.brandintegrationservice.repository.BrandRepository;
import com.fashionpin.brandintegrationservice.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class BrandControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminJwtStr;
    private String userJwtStr;

    @BeforeEach
    void setUp() {
        adminJwtStr = jwtService.generateAccessToken(UUID.randomUUID().toString(), "admin@fashionpin.com", List.of("ROLE_ADMIN"));
        userJwtStr = jwtService.generateAccessToken(UUID.randomUUID().toString(), "user@fashionpin.com", List.of("ROLE_USER"));
    }

    @Test
    void createBrand_Admin_Success() throws Exception {
        CreateBrandRequest request = CreateBrandRequest.builder()
                .name("Prada")
                .slug("prada")
                .description("Italian luxury fashion house")
                .website("https://prada.com")
                .status("ACTIVE")
                .build();

        mockMvc.perform(post("/api/brands")
                        .header("Authorization", "Bearer " + adminJwtStr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Prada"))
                .andExpect(jsonPath("$.data.slug").value("prada"));
    }

    @Test
    void createBrand_UserRole_Forbidden() throws Exception {
        CreateBrandRequest request = CreateBrandRequest.builder()
                .name("Unauthorized Brand")
                .slug("unauthorized")
                .build();

        mockMvc.perform(post("/api/brands")
                        .header("Authorization", "Bearer " + userJwtStr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBrandById_Public_Success() throws Exception {
        Brand brand = Brand.create("Channel", "channel", "Fashion brand", null, "https://chanel.com", "ACTIVE");
        brandRepository.save(brand);

        mockMvc.perform(get("/api/brands/" + brand.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Channel"));
    }
}
