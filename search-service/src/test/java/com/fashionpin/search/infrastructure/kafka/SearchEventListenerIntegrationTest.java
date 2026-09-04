package com.fashionpin.search.infrastructure.kafka;

import static org.mockito.Mockito.verify;

import com.fashionpin.common.event.FashionPostEventPayload;
import com.fashionpin.common.event.ProductEventPayload;
import com.fashionpin.search.service.FashionPostIndexSyncService;
import com.fashionpin.search.service.ProductIndexSyncService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchEventListenerIntegrationTest {

    @Mock
    private ProductIndexSyncService productSyncService;

    @Mock
    private FashionPostIndexSyncService fashionPostSyncService;

    private ProductSearchEventListener productListener;
    private FashionPostSearchEventListener fashionPostListener;

    @BeforeEach
    void setUp() {
        productListener = new ProductSearchEventListener(productSyncService);
        fashionPostListener = new FashionPostSearchEventListener(fashionPostSyncService);
    }

    @Test
    @DisplayName("ProductCreated event should route to ProductIndexSyncService.upsertProductIndex")
    void onProductCreated_shouldRouteToSyncService() {
        UUID productId = UUID.randomUUID();
        ProductEventPayload payload = ProductEventPayload.builder()
                .productId(productId)
                .title("Leather Jacket")
                .price(new BigDecimal("299.99"))
                .timestamp(Instant.now())
                .build();

        productListener.onProductCreated(payload);

        verify(productSyncService).upsertProductIndex(payload);
    }

    @Test
    @DisplayName("ProductDeleted event with UUID should route to ProductIndexSyncService.deleteProductIndex")
    void onProductDeleted_shouldRouteToSyncService() {
        UUID productId = UUID.randomUUID();

        productListener.onProductDeleted(productId);

        verify(productSyncService).deleteProductIndex(productId);
    }

    @Test
    @DisplayName("FashionPostCreated event should route to FashionPostIndexSyncService.upsertPostIndex")
    void onFashionPostCreated_shouldRouteToSyncService() {
        UUID postId = UUID.randomUUID();
        FashionPostEventPayload payload = FashionPostEventPayload.builder()
                .postId(postId)
                .caption("Summer vibe outfit")
                .style("Casual")
                .tags(List.of("summer", "casual"))
                .timestamp(Instant.now())
                .build();

        fashionPostListener.onFashionPostCreated(payload);

        verify(fashionPostSyncService).upsertPostIndex(payload);
    }

    @Test
    @DisplayName("FashionPostDeleted event should route to FashionPostIndexSyncService.deletePostIndex")
    void onFashionPostDeleted_shouldRouteToSyncService() {
        UUID postId = UUID.randomUUID();

        fashionPostListener.onFashionPostDeleted(postId);

        verify(fashionPostSyncService).deletePostIndex(postId);
    }
}
