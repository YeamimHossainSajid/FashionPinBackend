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
public class UpdateFashionPostRequest {
    private String outfitId;
    private String caption;
    private String visibility;
    private Style style;
    private Occasion occasion;
    private List<String> mediaIds;

    @Valid
    private List<FashionTagDto> tags;
}
