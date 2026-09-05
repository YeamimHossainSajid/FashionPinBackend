package com.fashionpin.fashiondiscoveryservice.service;

import com.fashionpin.common.event.FashionPostCreatedEvent;
import com.fashionpin.common.event.FashionPostDeletedEvent;
import com.fashionpin.common.event.FashionPostUpdatedEvent;
import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.fashiondiscoveryservice.dto.CreateFashionPostRequest;
import com.fashionpin.fashiondiscoveryservice.dto.FashionPostResponse;
import com.fashionpin.fashiondiscoveryservice.dto.FashionTagDto;
import com.fashionpin.fashiondiscoveryservice.dto.UpdateFashionPostRequest;
import com.fashionpin.fashiondiscoveryservice.entity.FashionPost;
import com.fashionpin.fashiondiscoveryservice.entity.FashionTag;
import com.fashionpin.fashiondiscoveryservice.entity.Outfit;
import com.fashionpin.fashiondiscoveryservice.kafka.FashionDiscoveryEventProducer;
import com.fashionpin.fashiondiscoveryservice.repository.FashionPostRepository;
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
public class FashionPostService {

    private final FashionPostRepository postRepository;
    private final OutfitRepository outfitRepository;
    private final OutfitService outfitService;
    private final FashionDiscoveryEventProducer eventProducer;

    @Transactional
    public FashionPostResponse createPost(String authorUserId, CreateFashionPostRequest request) {
        log.info("Creating fashion post for authorUserId: {}", authorUserId);

        Outfit outfit = null;
        if (request.getOutfitId() != null) {
            outfit = outfitRepository.findById(request.getOutfitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Outfit not found with id: " + request.getOutfitId()));
        }

        FashionPost post = FashionPost.builder()
                .authorUserId(authorUserId)
                .outfit(outfit)
                .caption(request.getCaption())
                .visibility(request.getVisibility() != null ? request.getVisibility() : "PUBLIC")
                .style(request.getStyle())
                .occasion(request.getOccasion())
                .mediaIds(request.getMediaIds() != null ? new ArrayList<>(request.getMediaIds()) : new ArrayList<>())
                .tags(new ArrayList<>())
                .build();

        if (request.getTags() != null) {
            for (FashionTagDto tagDto : request.getTags()) {
                FashionTag tag = FashionTag.builder()
                        .name(tagDto.getName())
                        .tagType(tagDto.getTagType())
                        .build();
                post.addTag(tag);
            }
        }

        FashionPost saved = postRepository.save(post);

        eventProducer.publishPostCreated(FashionPostCreatedEvent.create(
                saved.getId(),
                authorUserId,
                outfit != null ? outfit.getId() : null,
                saved.getMediaIds(),
                saved.getCaption(),
                null
        ));

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public FashionPostResponse getPostById(String id) {
        FashionPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fashion post not found with id: " + id));
        return mapToResponse(post);
    }

    @Transactional(readOnly = true)
    public Page<FashionPostResponse> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<FashionPostResponse> getPostsByProductId(String productId, Pageable pageable) {
        return postRepository.findByProductId(productId, pageable).map(this::mapToResponse);
    }

    @Transactional
    public FashionPostResponse updatePost(String id, String userId, UpdateFashionPostRequest request) {
        FashionPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fashion post not found with id: " + id));

        if (!post.getAuthorUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to update this fashion post");
        }

        if (request.getOutfitId() != null) {
            Outfit outfit = outfitRepository.findById(request.getOutfitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Outfit not found with id: " + request.getOutfitId()));
            post.setOutfit(outfit);
        }
        if (request.getCaption() != null) {
            post.setCaption(request.getCaption());
        }
        if (request.getVisibility() != null) {
            post.setVisibility(request.getVisibility());
        }
        if (request.getStyle() != null) {
            post.setStyle(request.getStyle());
        }
        if (request.getOccasion() != null) {
            post.setOccasion(request.getOccasion());
        }
        if (request.getMediaIds() != null) {
            post.setMediaIds(new ArrayList<>(request.getMediaIds()));
        }

        if (request.getTags() != null) {
            post.getTags().clear();
            for (FashionTagDto tagDto : request.getTags()) {
                FashionTag tag = FashionTag.builder()
                        .name(tagDto.getName())
                        .tagType(tagDto.getTagType())
                        .build();
                post.addTag(tag);
            }
        }

        FashionPost updated = postRepository.save(post);
        eventProducer.publishPostUpdated(FashionPostUpdatedEvent.create(updated.getId(), userId, null));

        return mapToResponse(updated);
    }

    @Transactional
    public void deletePost(String id, String userId) {
        FashionPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fashion post not found with id: " + id));

        if (!post.getAuthorUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to delete this fashion post");
        }

        postRepository.delete(post);
        eventProducer.publishPostDeleted(FashionPostDeletedEvent.create(id, userId, null));
    }

    public FashionPostResponse mapToResponse(FashionPost post) {
        List<FashionTagDto> tagDtos = post.getTags().stream()
                .map(tag -> FashionTagDto.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .tagType(tag.getTagType())
                        .build())
                .toList();

        return FashionPostResponse.builder()
                .id(post.getId())
                .authorUserId(post.getAuthorUserId())
                .outfit(post.getOutfit() != null ? outfitService.mapToResponse(post.getOutfit()) : null)
                .caption(post.getCaption())
                .visibility(post.getVisibility())
                .style(post.getStyle())
                .occasion(post.getOccasion())
                .mediaIds(post.getMediaIds())
                .tags(tagDtos)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
