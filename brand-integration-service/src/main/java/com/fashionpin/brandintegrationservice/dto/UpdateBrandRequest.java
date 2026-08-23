package com.fashionpin.brandintegrationservice.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBrandRequest {

    @Size(min = 2, max = 100, message = "Brand name must be between 2 and 100 characters")
    private String name;

    private String description;
    private String logoMediaId;
    private String website;
    private String status;
}
