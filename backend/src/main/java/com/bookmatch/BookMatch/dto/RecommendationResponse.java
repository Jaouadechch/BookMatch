package com.bookmatch.BookMatch.dto;

import java.util.List;

public record RecommendationResponse(
        MovieDto source,
        List<BookRecommendationDto> recommendations
) {
}