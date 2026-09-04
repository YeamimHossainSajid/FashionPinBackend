package com.fashionpin.search.dto;

import com.fashionpin.search.domain.model.FashionPostSearchIndex;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FashionPostSearchIndexDto {

    private UUID id;
    private UUID authorUserId;
    private String caption;
    private String style;
    private String occasion;
    private List<String> tags;
    private List<UUID> mediaIds;
    private Instant createdAt;

    public static FashionPostSearchIndexDto fromEntity(FashionPostSearchIndex entity) {
        if (entity == null) {
            return null;
        }
        return FashionPostSearchIndexDto.builder()
                .id(entity.getId())
                .authorUserId(entity.getAuthorUserId())
                .caption(entity.getCaption())
                .style(entity.getStyle())
                .occasion(entity.getOccasion())
                .tags(entity.getTags())
                .mediaIds(entity.getMediaIds())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
