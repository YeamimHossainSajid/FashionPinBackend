package com.fashionpin.fashiondiscoveryservice.dto;

import com.fashionpin.fashiondiscoveryservice.entity.Occasion;
import com.fashionpin.fashiondiscoveryservice.entity.Style;
import jakarta.validation.Valid;
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
public class CreateFashionPostRequest {

    private String outfitId;

    private String caption;

    @Builder.Default
    private String visibility = "PUBLIC";

    private Style style;

    private Occasion occasion;

    @NotEmpty(message = "At least one media ID is required")
    private List<String> mediaIds;

    @Valid
    private List<FashionTagDto> tags;
}
