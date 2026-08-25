package com.fashionpin.fashiondiscoveryservice.service;

import com.fashionpin.common.event.OutfitCreatedEvent;
import com.fashionpin.common.event.OutfitDeletedEvent;
import com.fashionpin.common.event.OutfitUpdatedEvent;
import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.fashiondiscoveryservice.dto.CreateOutfitRequest;
import com.fashionpin.fashiondiscoveryservice.dto.OutfitItemDto;
import com.fashionpin.fashiondiscoveryservice.dto.OutfitResponse;
import com.fashionpin.fashiondiscoveryservice.dto.UpdateOutfitRequest;
import com.fashionpin.fashiondiscoveryservice.entity.Outfit;
import com.fashionpin.fashiondiscoveryservice.entity.OutfitItem;
import com.fashionpin.fashiondiscoveryservice.kafka.FashionDiscoveryEventProducer;
import com.fashionpin.fashiondiscoveryservice.repository.OutfitRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutfitService {

    private final OutfitRepository outfitRepository;
    private final FashionDiscoveryEventProducer eventProducer;

    @Transactional
    public OutfitResponse createOutfit(String authorUserId, CreateOutfitRequest request) {
        log.info("Creating outfit for userId: {}", authorUserId);

        Outfit outfit = Outfit.builder()
                .authorUserId(authorUserId)
                .name(request.getName())
                .description(request.getDescription())
                .style(request.getStyle())
                .occasion(request.getOccasion())
                .items(new ArrayList<>())
                .build();

        if (request.getItems() != null) {
            for (OutfitItemDto itemDto : request.getItems()) {
                OutfitItem item = OutfitItem.builder()
                        .productId(itemDto.getProductId())
                        .category(itemDto.getCategory())
                        .positionIndex(itemDto.getPositionIndex())
                        .build();
                outfit.addItem(item);
            }
        }

        Outfit saved = outfitRepository.save(outfit);

        List<String> productIds = saved.getItems().stream().map(OutfitItem::getProductId).toList();
        eventProducer.publishOutfitCreated(OutfitCreatedEvent.create(
                saved.getId(), authorUserId, saved.getName(), productIds, null));

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public OutfitResponse getOutfitById(String id) {
        Outfit outfit = outfitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Outfit not found with id: " + id));
        return mapToResponse(outfit);
    }

    @Transactional(readOnly = true)
    public Page<OutfitResponse> getOutfitsByAuthor(String authorUserId, Pageable pageable) {
        return outfitRepository.findByAuthorUserId(authorUserId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public OutfitResponse updateOutfit(String id, String userId, UpdateOutfitRequest request) {
        Outfit outfit = outfitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Outfit not found with id: " + id));

        if (!outfit.getAuthorUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to update this outfit");
        }

        if (request.getName() != null) {
            outfit.setName(request.getName());
        }
        if (request.getDescription() != null) {
            outfit.setDescription(request.getDescription());
        }
        if (request.getStyle() != null) {
            outfit.setStyle(request.getStyle());
        }
        if (request.getOccasion() != null) {
            outfit.setOccasion(request.getOccasion());
        }

        if (request.getItems() != null) {
            outfit.getItems().clear();
            for (OutfitItemDto itemDto : request.getItems()) {
                OutfitItem item = OutfitItem.builder()
                        .productId(itemDto.getProductId())
                        .category(itemDto.getCategory())
                        .positionIndex(itemDto.getPositionIndex())
                        .build();
                outfit.addItem(item);
            }
        }

        Outfit updated = outfitRepository.save(outfit);
        eventProducer.publishOutfitUpdated(OutfitUpdatedEvent.create(updated.getId(), userId, null));

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteOutfit(String id, String userId) {
        Outfit outfit = outfitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Outfit not found with id: " + id));

        if (!outfit.getAuthorUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to delete this outfit");
        }

        outfitRepository.delete(outfit);
        eventProducer.publishOutfitDeleted(OutfitDeletedEvent.create(id, userId, null));
    }

    public OutfitResponse mapToResponse(Outfit outfit) {
        List<OutfitItemDto> itemDtos = outfit.getItems().stream()
                .map(item -> OutfitItemDto.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .category(item.getCategory())
                        .positionIndex(item.getPositionIndex())
                        .build())
                .toList();

        return OutfitResponse.builder()
                .id(outfit.getId())
                .authorUserId(outfit.getAuthorUserId())
                .name(outfit.getName())
                .description(outfit.getDescription())
                .style(outfit.getStyle())
                .occasion(outfit.getOccasion())
                .items(itemDtos)
                .createdAt(outfit.getCreatedAt())
                .updatedAt(outfit.getUpdatedAt())
                .build();
    }
}
