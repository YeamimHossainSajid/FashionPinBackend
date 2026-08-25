package com.fashionpin.fashiondiscoveryservice.dto;

import com.fashionpin.fashiondiscoveryservice.entity.Occasion;
import com.fashionpin.fashiondiscoveryservice.entity.Style;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOutfitRequest {
    private String name;
    private String description;
    private Style style;
    private Occasion occasion;

    @Valid
    private List<OutfitItemDto> items;
}
