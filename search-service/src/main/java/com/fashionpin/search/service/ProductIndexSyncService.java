package com.fashionpin.search.service;

import com.fashionpin.common.event.ProductEventPayload;
import com.fashionpin.search.domain.model.ProductSearchIndex;
import com.fashionpin.search.repository.ProductSearchIndexRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductIndexSyncService {

    private final ProductSearchIndexRepository repository;

    @Transactional
    public void upsertProductIndex(ProductEventPayload payload) {
        if (payload == null || payload.getProductId() == null) {
            log.warn("Received null or invalid ProductEventPayload for search index sync");
            return;
        }

        UUID productId = payload.getProductId();
        Optional<ProductSearchIndex> existingOpt = repository.findById(productId);

        if (existingOpt.isPresent()) {
            ProductSearchIndex existing = existingOpt.get();
            Instant eventTime = payload.getTimestamp() != null ? payload.getTimestamp() : Instant.now();
            if (existing.getUpdatedAt() != null && eventTime.isBefore(existing.getUpdatedAt())) {
                log.warn("Discarding out-of-order ProductEvent for productId={}. Event timestamp ({}) is before existing updatedAt ({})",
                        productId, eventTime, existing.getUpdatedAt());
                return;
            }
            log.info("Updating ProductSearchIndex for productId={}", productId);
            existing.updateFromPayload(payload);
            repository.save(existing);
        } else {
            log.info("Creating new ProductSearchIndex for productId={}", productId);
            ProductSearchIndex newIndex = ProductSearchIndex.fromPayload(payload);
            repository.save(newIndex);
        }
    }

    @Transactional
    public void deleteProductIndex(UUID productId) {
        if (productId == null) {
            return;
        }
        if (repository.existsById(productId)) {
            log.info("Deleting ProductSearchIndex for productId={}", productId);
            repository.deleteById(productId);
        } else {
            log.debug("ProductSearchIndex not found for deletion, productId={}", productId);
        }
    }
}
