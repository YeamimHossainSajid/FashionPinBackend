package com.fashionpin.fashiondiscoveryservice.dto;

import com.fashionpin.fashiondiscoveryservice.entity.Occasion;
import com.fashionpin.fashiondiscoveryservice.entity.Style;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FashionPostResponse {
    private String id;
    private String authorUserId;
    private OutfitResponse outfit;
    private String caption;
    private String visibility;
    private Style style;
    private Occasion occasion;
    private List<String> mediaIds;
    private List<FashionTagDto> tags;
    private Instant createdAt;
    private Instant updatedAt;
}
