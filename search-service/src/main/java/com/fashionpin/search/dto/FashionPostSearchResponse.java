package com.fashionpin.search.dto;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FashionPostSearchResponse {

    @Builder.Default
    private List<FashionPostSearchIndexDto> items = Collections.emptyList();

    private int page;
    private int pageSize;
    private long totalElements;
    private int totalPages;
}
