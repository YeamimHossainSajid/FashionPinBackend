package com.fashionpin.fashiondiscoveryservice.ranking;

import com.fashionpin.fashiondiscoveryservice.dto.DiscoveryFilter;
import com.fashionpin.fashiondiscoveryservice.entity.FashionPost;
import java.util.List;

public interface DiscoveryRankingStrategy {
    List<FashionPost> rank(List<FashionPost> posts, DiscoveryFilter filter);
}
