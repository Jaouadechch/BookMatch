package com.bookmatch.BookMatch.dto;

import com.bookmatch.BookMatch.entity.Book;

public record BookRecommendationDto(
        Long id,
        String title,
        String author,
        String description,
        String genre,
        String tags,
        double rating,
        String coverUrl,
        int matchScore,
        String matchReason
) {

    public static BookRecommendationDto from(
            Book book,
            AiMatchDto aiMatch
    ) {
        return new BookRecommendationDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getGenre(),
                book.getTags(),
                book.getRating(),
                book.getCoverUrl(),
                aiMatch.matchScore(),
                aiMatch.matchReason()
        );
    }
}