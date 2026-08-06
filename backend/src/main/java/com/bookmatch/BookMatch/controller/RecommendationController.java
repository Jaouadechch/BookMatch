package com.bookmatch.BookMatch.controller;

import com.bookmatch.BookMatch.dto.RecommendationResponse;
import com.bookmatch.BookMatch.entity.Book;
import com.bookmatch.BookMatch.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * Complete response:
     * movie information + recommended books.
     */
    @GetMapping("/full")
    public RecommendationResponse getFullRecommendation(
            @RequestParam String movie
    ) {
        return recommendationService
                .getFullRecommendation(movie);
    }

    /**
     * Returns only recommended books.
     */
    @GetMapping
    public List<Book> recommendBooks(
            @RequestParam String movie
    ) {
        return recommendationService
                .recommendBooks(movie);
    }

    /**
     * Returns every book stored in PostgreSQL.
     */
    @GetMapping("/all")
    public List<Book> getAllBooks() {
        return recommendationService.getAllBooks();
    }
}