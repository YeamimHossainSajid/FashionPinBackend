# Part 3: Search Query Engine, Specifications, Facets & REST API

## Summary

Part 3 implements the high-performance search querying engine inside `search-service` (port `8088`), featuring dynamic multi-criteria JPA specifications, database-agnostic faceted aggregations, typeahead suggestions, and the full REST controller under `/api/search/**`.

## Components

### 1. DTOs & Query Request Models (`com.fashionpin.search.dto`)
- `ProductSearchQueryCriteria`: Full query criteria model supporting text query, brand ID, category, subcategory, color, size, price bounds, in-stock filter, sort resolution, and pagination.
- `FashionPostSearchQueryCriteria`: Query criteria for fashion post discovery supporting text query, style, occasion, tag, author user ID, and pagination.
- `SearchSortBy`: Enum supporting `RELEVANCE` (default), `PRICE_ASC`, `PRICE_DESC`, `NEWEST`.
- `ProductSearchIndexDto`: API representation with static `fromEntity()` mapper.
- `FashionPostSearchIndexDto`: API representation with static `fromEntity()` mapper.
- `SearchFacetResultDto`: Facet aggregation container for brands, categories, colors, sizes, and price bounds.
- `ProductSearchResponse`: Paginated product response with embedded facet distribution.
- `FashionPostSearchResponse`: Paginated post response.
- `SearchSuggestionResponse`: Autocomplete response for suggestions, categories, and tags.

### 2. Dynamic JPA Specifications (`com.fashionpin.search.service.specification`)
- `ProductSearchSpecifications`:
  - Case-insensitive text search (`title`, `description`, `tags`).
  - Equality filters for `brandId`, `category`, `subcategory`.
  - Range filters for `minPrice` and `maxPrice`.
  - Color and size containment matching.
  - Stock filter `inStock = :inStock`.
  - Sort mapping for `PRICE_ASC`, `PRICE_DESC`, `NEWEST`, `RELEVANCE`.
- `FashionPostSearchSpecifications`:
  - Case-insensitive text matching on `caption` and `tags`.
  - Equality filters for `style`, `occasion`, `authorUserId`.
  - Tag containment matching.
  - Default sort on `createdAt DESC`.

### 3. Faceted Aggregation & Autocomplete Service (`com.fashionpin.search.service`)
- `SearchCatalogService`:
  - `searchProducts`: Applies dynamic specifications, sorting, pagination, and computes facets.
  - `searchPosts`: Applies post specifications, sorting, and pagination.
  - `computeFacets`: Computes brand counts, category counts, min/max price bounds via CriteriaBuilder, and extracts color/size distributions.
  - `getSuggestions`: Validates minimum prefix length (>= 2 characters) and aggregates matching distinct titles, categories, and tags.

### 4. REST Controller & Exception Handling (`com.fashionpin.search.controller`)
- `SearchController` (`/api/search`):
  - `GET /products`: Multi-criteria product search with facets.
  - `GET /posts`: Post discovery search.
  - `GET /suggestions`: Typeahead suggestions.
  - `GET /facets`: Standalone facet computation endpoint.
- `GlobalExceptionHandler`: Enhanced with handlers for `IllegalArgumentException`, `MethodArgumentTypeMismatchException`, and `ConstraintViolationException` returning HTTP 400 with standardized error responses.
- `SecurityConfig`: Permitted public GET requests to `/api/search/**`.

### 5. Test Suite (`search-service/src/test/java`)
- `SearchSpecificationsTest`: Verifies composite predicate generation and sort resolution.
- `SearchCatalogServiceTest`: Unit tests for pagination math, facet aggregation assembly, and prefix query length constraints.
- `SearchControllerIntegrationTest`: MockMvc integration tests for HTTP 200 responses, query parameter parsing, and HTTP 400 validation failures.
