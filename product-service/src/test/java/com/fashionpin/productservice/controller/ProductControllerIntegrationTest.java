package com.fashionpin.productservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fashionpin.productservice.dto.CreateProductRequest;
import com.fashionpin.productservice.dto.UpdateProductRequest;
import com.fashionpin.productservice.entity.Product;
import com.fashionpin.productservice.repository.ProductRepository;
import com.fashionpin.productservice.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminJwtStr;
    private String userJwtStr;
    private String brandId;

    @BeforeEach
    void setUp() {
        adminJwtStr = jwtService.generateAccessToken(UUID.randomUUID().toString(), "admin@fashionpin.com", List.of("ROLE_ADMIN"));
        userJwtStr = jwtService.generateAccessToken(UUID.randomUUID().toString(), "user@fashionpin.com", List.of("ROLE_USER"));
        brandId = UUID.randomUUID().toString();
    }

    @Test
    void createProduct_Admin_Success() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .brandId(brandId)
                .name("Leather Jacket")
                .slug("leather-jacket")
                .category("Outerwear")
                .productType("Jacket")
                .price(new BigDecimal("499.00"))
                .currency("USD")
                .gender("Unisex")
                .color("Black")
                .size("L")
                .build();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminJwtStr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Leather Jacket"))
                .andExpect(jsonPath("$.data.category").value("Outerwear"));
    }

    @Test
    void createProduct_UserRole_Forbidden() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .brandId(brandId)
                .name("Unauthorized Product")
                .category("Shoes")
                .price(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + userJwtStr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProducts_Paginated_Public_Success() throws Exception {
        Product product = Product.create(
                brandId, "Denim Jeans", "denim-jeans", "Classic jeans",
                "Bottoms", "Jeans", "Pants", new BigDecimal("89.99"), "USD",
                null, null, "Men", "Blue", "32", "Denim", null, "Casual", "All", "Everyday", "Slim"
        );
        productRepository.save(product);

        mockMvc.perform(get("/api/products?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Denim Jeans"));
    }
}
