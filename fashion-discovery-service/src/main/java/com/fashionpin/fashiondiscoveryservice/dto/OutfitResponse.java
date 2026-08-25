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
public class OutfitResponse {
    private String id;
    private String authorUserId;
    private String name;
    private String description;
    private Style style;
    private Occasion occasion;
    private List<OutfitItemDto> items;
    private Instant createdAt;
    private Instant updatedAt;
}
