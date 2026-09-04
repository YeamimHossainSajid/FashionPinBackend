package com.fashionpin.search.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FashionPostSearchQueryCriteria {

    private String query;
    private String style;
    private String occasion;
    private String tag;
    private UUID authorUserId;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int pageSize = 20;
}
