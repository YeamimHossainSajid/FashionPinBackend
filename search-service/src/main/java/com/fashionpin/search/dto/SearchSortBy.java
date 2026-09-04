package com.fashionpin.search.dto;

public enum SearchSortBy {
    RELEVANCE,
    PRICE_ASC,
    PRICE_DESC,
    NEWEST;

    public static SearchSortBy fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return RELEVANCE;
        }
        for (SearchSortBy sortBy : values()) {
            if (sortBy.name().equalsIgnoreCase(value.trim())) {
                return sortBy;
            }
        }
        throw new IllegalArgumentException("Invalid sortBy parameter: " + value + ". Allowed values: RELEVANCE, PRICE_ASC, PRICE_DESC, NEWEST");
    }
}
