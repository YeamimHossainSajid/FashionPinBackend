package com.fashionpin.fashiondiscoveryservice.service;

import com.fashionpin.fashiondiscoveryservice.dto.DiscoveryFilter;
import com.fashionpin.fashiondiscoveryservice.dto.FashionPostResponse;
import com.fashionpin.fashiondiscoveryservice.entity.FashionPost;
import com.fashionpin.fashiondiscoveryservice.ranking.DiscoveryRankingStrategy;
import com.fashionpin.fashiondiscoveryservice.repository.FashionPostRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoveryFeedService {

    private final FashionPostRepository postRepository;
    private final DiscoveryRankingStrategy rankingStrategy;
    private final FashionPostService postService;

    @Transactional(readOnly = true)
    public Page<FashionPostResponse> discover(DiscoveryFilter filter, Pageable pageable) {
        log.info("Executing discovery feed search with filter: {}", filter);

        Page<FashionPost> rawPostsPage = postRepository.findDiscoveryPosts(
                filter.getStyle(),
                filter.getOccasion(),
                filter.getColor(),
                filter.getSeason(),
                pageable
        );

        List<FashionPost> rankedPosts = rankingStrategy.rank(rawPostsPage.getContent(), filter);
        List<FashionPostResponse> responseDtos = rankedPosts.stream()
                .map(postService::mapToResponse)
                .toList();

        return new PageImpl<>(responseDtos, pageable, rawPostsPage.getTotalElements());
    }
}
