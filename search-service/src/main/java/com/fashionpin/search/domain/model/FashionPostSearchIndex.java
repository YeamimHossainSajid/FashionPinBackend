package com.fashionpin.search.domain.model;

import com.fashionpin.common.event.FashionPostEventPayload;
import com.fashionpin.search.domain.model.converter.StringListConverter;
import com.fashionpin.search.domain.model.converter.UuidListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fashion_post_search_index")
public class FashionPostSearchIndex {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @Column(name = "caption", columnDefinition = "TEXT")
    private String caption;

    @Column(name = "style")
    private String style;

    @Column(name = "occasion")
    private String occasion;

    @Convert(converter = StringListConverter.class)
    @Column(name = "tags")
    private List<String> tags;

    @Convert(converter = UuidListConverter.class)
    @Column(name = "media_ids")
    private List<UUID> mediaIds;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static FashionPostSearchIndex fromPayload(FashionPostEventPayload payload) {
        if (payload == null) {
            return null;
        }
        Instant now = payload.getTimestamp() != null ? payload.getTimestamp() : Instant.now();
        return FashionPostSearchIndex.builder()
                .id(payload.getPostId())
                .authorUserId(payload.getAuthorUserId())
                .caption(payload.getCaption())
                .style(payload.getStyle())
                .occasion(payload.getOccasion())
                .tags(payload.getTags())
                .mediaIds(payload.getMediaIds())
                .createdAt(now)
                .build();
    }

    public void updateFromPayload(FashionPostEventPayload payload) {
        if (payload == null) {
            return;
        }
        if (payload.getAuthorUserId() != null) {
            this.authorUserId = payload.getAuthorUserId();
        }
        if (payload.getCaption() != null) {
            this.caption = payload.getCaption();
        }
        if (payload.getStyle() != null) {
            this.style = payload.getStyle();
        }
        if (payload.getOccasion() != null) {
            this.occasion = payload.getOccasion();
        }
        if (payload.getTags() != null) {
            this.tags = payload.getTags();
        }
        if (payload.getMediaIds() != null) {
            this.mediaIds = payload.getMediaIds();
        }
    }
}
