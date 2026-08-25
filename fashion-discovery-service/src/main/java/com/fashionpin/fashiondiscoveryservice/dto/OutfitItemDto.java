package com.fashionpin.fashiondiscoveryservice.dto;

import com.fashionpin.fashiondiscoveryservice.entity.OutfitItemCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutfitItemDto {
    private String id;

    @NotBlank(message = "productId is required")
    private String productId;

    @NotNull(message = "category is required")
    private OutfitItemCategory category;

    private Integer positionIndex;
}
