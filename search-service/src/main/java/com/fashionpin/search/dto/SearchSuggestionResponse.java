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
public class SearchSuggestionResponse {

    @Builder.Default
    private List<String> suggestions = Collections.emptyList();

    @Builder.Default
    private List<String> categories = Collections.emptyList();

    @Builder.Default
    private List<String> tags = Collections.emptyList();
}
