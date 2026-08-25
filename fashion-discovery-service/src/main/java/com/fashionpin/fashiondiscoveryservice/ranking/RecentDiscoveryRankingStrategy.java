package com.fashionpin.fashiondiscoveryservice.ranking;

import com.fashionpin.fashiondiscoveryservice.dto.DiscoveryFilter;
import com.fashionpin.fashiondiscoveryservice.entity.FashionPost;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecentDiscoveryRankingStrategy implements DiscoveryRankingStrategy {

    @Override
    public List<FashionPost> rank(List<FashionPost> posts, DiscoveryFilter filter) {
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }
        // Deterministic/recent ordering placeholder: newest created first
        return posts.stream()
                .sorted(Comparator.comparing(FashionPost::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
