package com.fashionpin.search.service;

import com.fashionpin.common.event.FashionPostEventPayload;
import com.fashionpin.search.domain.model.FashionPostSearchIndex;
import com.fashionpin.search.repository.FashionPostSearchIndexRepository;
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
public class FashionPostIndexSyncService {

    private final FashionPostSearchIndexRepository repository;

    @Transactional
    public void upsertPostIndex(FashionPostEventPayload payload) {
        if (payload == null || payload.getPostId() == null) {
            log.warn("Received null or invalid FashionPostEventPayload for search index sync");
            return;
        }

        UUID postId = payload.getPostId();
        Optional<FashionPostSearchIndex> existingOpt = repository.findById(postId);

        if (existingOpt.isPresent()) {
            FashionPostSearchIndex existing = existingOpt.get();
            Instant eventTime = payload.getTimestamp() != null ? payload.getTimestamp() : Instant.now();
            if (existing.getCreatedAt() != null && eventTime.isBefore(existing.getCreatedAt())) {
                log.warn("Discarding out-of-order FashionPostEvent for postId={}. Event timestamp ({}) is before existing createdAt ({})",
                        postId, eventTime, existing.getCreatedAt());
                return;
            }
            log.info("Updating FashionPostSearchIndex for postId={}", postId);
            existing.updateFromPayload(payload);
            repository.save(existing);
        } else {
            log.info("Creating new FashionPostSearchIndex for postId={}", postId);
            FashionPostSearchIndex newIndex = FashionPostSearchIndex.fromPayload(payload);
            repository.save(newIndex);
        }
    }

    @Transactional
    public void deletePostIndex(UUID postId) {
        if (postId == null) {
            return;
        }
        if (repository.existsById(postId)) {
            log.info("Deleting FashionPostSearchIndex for postId={}", postId);
            repository.deleteById(postId);
        } else {
            log.debug("FashionPostSearchIndex not found for deletion, postId={}", postId);
        }
    }
}
