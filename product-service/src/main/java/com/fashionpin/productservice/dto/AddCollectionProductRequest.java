package com.fashionpin.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCollectionProductRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    private Integer displayOrder;
    private String curatorNote;
}
