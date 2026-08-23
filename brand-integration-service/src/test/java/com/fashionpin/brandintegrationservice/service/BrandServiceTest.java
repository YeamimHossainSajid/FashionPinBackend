package com.fashionpin.brandintegrationservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fashionpin.brandintegrationservice.dto.BrandResponse;
import com.fashionpin.brandintegrationservice.dto.CreateBrandRequest;
import com.fashionpin.brandintegrationservice.dto.UpdateBrandRequest;
import com.fashionpin.brandintegrationservice.entity.Brand;
import com.fashionpin.brandintegrationservice.repository.BrandRepository;
import com.fashionpin.common.exception.BusinessException;
import com.fashionpin.common.exception.ResourceNotFoundException;
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
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private BrandService brandService;

    private Brand sampleBrand;

    @BeforeEach
    void setUp() {
        sampleBrand = Brand.builder()
                .id(UUID.randomUUID().toString())
                .name("Gucci")
                .slug("gucci")
                .description("Luxury fashion house")
                .logoMediaId("media-123")
                .website("https://gucci.com")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void createBrand_Success() {
        CreateBrandRequest request = CreateBrandRequest.builder()
                .name("Gucci")
                .slug("gucci")
                .description("Luxury fashion house")
                .website("https://gucci.com")
                .status("ACTIVE")
                .build();

        when(brandRepository.existsBySlug("gucci")).thenReturn(false);
        when(brandRepository.save(any(Brand.class))).thenAnswer(i -> i.getArgument(0));

        BrandResponse response = brandService.createBrand(request);

        assertNotNull(response);
        assertEquals("Gucci", response.getName());
        assertEquals("gucci", response.getSlug());
        verify(outboxService, times(1)).saveEvent(eq("Brand"), any(), eq("BrandCreated"), any());
    }

    @Test
    void createBrand_DuplicateSlug_ThrowsException() {
        CreateBrandRequest request = CreateBrandRequest.builder()
                .name("Gucci Duplicate")
                .slug("gucci")
                .build();

        when(brandRepository.existsBySlug("gucci")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> brandService.createBrand(request));
        assertEquals("BRAND_SLUG_ALREADY_EXISTS", ex.getCode());
    }

    @Test
    void getBrandById_Success() {
        when(brandRepository.findById(sampleBrand.getId())).thenReturn(Optional.of(sampleBrand));

        BrandResponse response = brandService.getBrandById(sampleBrand.getId());

        assertNotNull(response);
        assertEquals("Gucci", response.getName());
    }

    @Test
    void updateBrand_Success() {
        UpdateBrandRequest request = UpdateBrandRequest.builder()
                .name("Gucci Updated")
                .description("Updated description")
                .build();

        when(brandRepository.findById(sampleBrand.getId())).thenReturn(Optional.of(sampleBrand));
        when(brandRepository.save(any(Brand.class))).thenAnswer(i -> i.getArgument(0));

        BrandResponse response = brandService.updateBrand(sampleBrand.getId(), request);

        assertNotNull(response);
        assertEquals("Gucci Updated", response.getName());
        verify(outboxService, times(1)).saveEvent(eq("Brand"), eq(sampleBrand.getId()), eq("BrandUpdated"), any());
    }

    @Test
    void deleteBrand_Success() {
        when(brandRepository.findById(sampleBrand.getId())).thenReturn(Optional.of(sampleBrand));

        brandService.deleteBrand(sampleBrand.getId());

        verify(brandRepository, times(1)).delete(sampleBrand);
        verify(outboxService, times(1)).saveEvent(eq("Brand"), eq(sampleBrand.getId()), eq("BrandDeleted"), any());
    }
}
