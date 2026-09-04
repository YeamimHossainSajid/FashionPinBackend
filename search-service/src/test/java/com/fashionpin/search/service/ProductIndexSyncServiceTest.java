package com.fashionpin.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fashionpin.common.event.ProductEventPayload;
import com.fashionpin.search.domain.model.ProductSearchIndex;
import com.fashionpin.search.repository.ProductSearchIndexRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductIndexSyncServiceTest {

    @Mock
    private ProductSearchIndexRepository repository;

    @InjectMocks
    private ProductIndexSyncService syncService;

    @Captor
    private ArgumentCaptor<ProductSearchIndex> indexCaptor;

    private UUID productId;
    private UUID brandId;
    private Instant now;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        brandId = UUID.randomUUID();
        now = Instant.now();
    }

    @Test
    @DisplayName("Should create new ProductSearchIndex when entity does not exist in DB")
    void upsertProductIndex_whenNewProduct_shouldSaveNewEntity() {
        ProductEventPayload payload = ProductEventPayload.builder()
                .productId(productId)
                .brandId(brandId)
                .title("Silk Dress")
                .description("Elegant silk dress")
                .category("Dresses")
                .subcategory("Evening")
                .colors(List.of("Red", "Black"))
                .sizes(List.of("S", "M"))
                .price(new BigDecimal("199.99"))
                .currency("USD")
                .tags(List.of("silk", "evening"))
                .inStock(true)
                .timestamp(now)
                .build();

        when(repository.findById(productId)).thenReturn(Optional.empty());

        syncService.upsertProductIndex(payload);

        verify(repository).save(indexCaptor.capture());
        ProductSearchIndex saved = indexCaptor.getValue();

        assertNotNull(saved);
        assertEquals(productId, saved.getId());
        assertEquals(brandId, saved.getBrandId());
        assertEquals("Silk Dress", saved.getTitle());
        assertEquals("Dresses", saved.getCategory());
        assertTrue(saved.isInStock());
        assertEquals(new BigDecimal("199.99"), saved.getPriceAmount());
    }

    @Test
    @DisplayName("Should update existing ProductSearchIndex when event timestamp is newer")
    void upsertProductIndex_whenExistingProductWithNewerTimestamp_shouldUpdateEntity() {
        Instant earlierTime = now.minusSeconds(3600);
        Instant newerTime = now;

        ProductSearchIndex existing = ProductSearchIndex.builder()
                .id(productId)
                .brandId(brandId)
                .title("Old Title")
                .category("Dresses")
                .priceAmount(new BigDecimal("100.00"))
                .priceCurrency("USD")
                .inStock(true)
                .createdAt(earlierTime)
                .updatedAt(earlierTime)
                .build();

        ProductEventPayload payload = ProductEventPayload.builder()
                .productId(productId)
                .brandId(brandId)
                .title("Updated Title")
                .category("Dresses")
                .price(new BigDecimal("150.00"))
                .currency("USD")
                .inStock(false)
                .timestamp(newerTime)
                .build();

        when(repository.findById(productId)).thenReturn(Optional.of(existing));

        syncService.upsertProductIndex(payload);

        verify(repository).save(existing);
        assertEquals("Updated Title", existing.getTitle());
        assertEquals(new BigDecimal("150.00"), existing.getPriceAmount());
        assertFalse(existing.isInStock());
        assertEquals(newerTime, existing.getUpdatedAt());
    }

    @Test
    @DisplayName("Should discard out-of-order event when event timestamp is older than existing updatedAt")
    void upsertProductIndex_whenExistingProductWithOlderTimestamp_shouldDiscardOutOfOrder() {
        Instant currentTime = now;
        Instant olderTime = now.minusSeconds(3600);

        ProductSearchIndex existing = ProductSearchIndex.builder()
                .id(productId)
                .brandId(brandId)
                .title("Latest Title")
                .category("Dresses")
                .priceAmount(new BigDecimal("200.00"))
                .priceCurrency("USD")
                .inStock(true)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();

        ProductEventPayload payload = ProductEventPayload.builder()
                .productId(productId)
                .brandId(brandId)
                .title("Stale Title")
                .category("Dresses")
                .price(new BigDecimal("50.00"))
                .currency("USD")
                .inStock(false)
                .timestamp(olderTime)
                .build();

        when(repository.findById(productId)).thenReturn(Optional.of(existing));

        syncService.upsertProductIndex(payload);

        verify(repository, never()).save(any());
        assertEquals("Latest Title", existing.getTitle());
        assertEquals(new BigDecimal("200.00"), existing.getPriceAmount());
    }

    @Test
    @DisplayName("Should delete ProductSearchIndex idempotently when entity exists")
    void deleteProductIndex_whenProductExists_shouldDeleteEntity() {
        when(repository.existsById(productId)).thenReturn(true);

        syncService.deleteProductIndex(productId);

        verify(repository).deleteById(productId);
    }
}
