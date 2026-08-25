package com.fashionpin.fashiondiscoveryservice.dto;

import com.fashionpin.fashiondiscoveryservice.entity.Occasion;
import com.fashionpin.fashiondiscoveryservice.entity.Style;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryFilter {
    private Style style;
    private Occasion occasion;
    private String category;
    private String color;
    private String season;
}
