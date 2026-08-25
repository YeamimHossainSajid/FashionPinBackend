package com.fashionpin.fashiondiscoveryservice.dto;

import com.fashionpin.fashiondiscoveryservice.entity.Occasion;
import com.fashionpin.fashiondiscoveryservice.entity.Style;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutfitRequest {

    @NotBlank(message = "Outfit name is required")
    private String name;

    private String description;
    private Style style;
    private Occasion occasion;

    @Valid
    @NotEmpty(message = "Outfit must contain at least one item")
    private List<OutfitItemDto> items;
}
