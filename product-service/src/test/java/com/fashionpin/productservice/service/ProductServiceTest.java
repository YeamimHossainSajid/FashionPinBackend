package com.fashionpin.productservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.common.exception.BusinessException;
import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.productservice.client.BrandServiceClient;
import com.fashionpin.productservice.dto.BrandDto;
import com.fashionpin.productservice.dto.CreateProductRequest;
import com.fashionpin.productservice.dto.ProductResponse;
import com.fashionpin.productservice.dto.UpdateProductRequest;
import com.fashionpin.productservice.entity.Product;
import com.fashionpin.productservice.repository.CategoryRepository;
import com.fashionpin.productservice.repository.ItemTypeRepository;
import com.fashionpin.productservice.repository.ProductRepository;
import com.fashionpin.productservice.repository.ProductVariantRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ItemTypeRepository itemTypeRepository;

    @Mock
    private BrandServiceClient brandServiceClient;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private String brandId;

    @BeforeEach
    void setUp() {
        brandId = UUID.randomUUID().toString();
        sampleProduct = Product.builder()
                .id(UUID.randomUUID().toString())
                .brandId(brandId)
                .name("Silk Floral Dress")
                .slug("silk-floral-dress")
                .description("Elegant silk dress")
                .category("Dresses")
                .productType("Maxi Dress")
                .status("ACTIVE")
                .price(new BigDecimal("299.99"))
                .currency("USD")
                .gender("Female")
                .color("Red")
                .size("M")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void createProduct_Success() {
        CreateProductRequest request = CreateProductRequest.builder()
                .brandId(brandId)
                .name("Silk Floral Dress")
                .category("Dresses")
                .price(new BigDecimal("299.99"))
                .currency("USD")
                .gender("Female")
                .color("Red")
                .size("M")
                .build();

        BrandDto brandDto = BrandDto.builder().id(brandId).name("Gucci").status("ACTIVE").build();
        when(brandServiceClient.getBrandById(brandId)).thenReturn(ApiResponse.ok(brandDto));
        when(productRepository.existsBySlug("silk-floral-dress")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("Silk Floral Dress", response.getName());
        assertEquals("Dresses", response.getCategory());
        verify(outboxService, times(1)).saveEvent(eq("Product"), any(), eq("ProductCreated"), any());
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(sampleProduct.getId())).thenReturn(Optional.of(sampleProduct));

        ProductResponse response = productService.getProductById(sampleProduct.getId());

        assertNotNull(response);
        assertEquals(sampleProduct.getId(), response.getId());
        assertEquals("Silk Floral Dress", response.getName());
    }

    @Test
    void getProducts_Pagination_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);

        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<ProductResponse> result = productService.getProducts(null, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Silk Floral Dress", result.getContent().get(0).getName());
    }

    @Test
    void updateProduct_Success() {
        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("Updated Dress Name")
                .price(new BigDecimal("349.99"))
                .build();

        when(productRepository.findById(sampleProduct.getId())).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductResponse response = productService.updateProduct(sampleProduct.getId(), request);

        assertNotNull(response);
        assertEquals("Updated Dress Name", response.getName());
        assertEquals(new BigDecimal("349.99"), response.getPrice());
        verify(outboxService, times(1)).saveEvent(eq("Product"), eq(sampleProduct.getId()), eq("ProductUpdated"), any());
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.findById(sampleProduct.getId())).thenReturn(Optional.of(sampleProduct));

        productService.deleteProduct(sampleProduct.getId());

        verify(productRepository, times(1)).delete(sampleProduct);
        verify(outboxService, times(1)).saveEvent(eq("Product"), eq(sampleProduct.getId()), eq("ProductDeleted"), any());
    }
}
