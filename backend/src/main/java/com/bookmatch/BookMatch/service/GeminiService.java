package com.bookmatch.BookMatch.service;

import com.bookmatch.BookMatch.dto.AiMatchDto;
import com.bookmatch.BookMatch.dto.MovieDto;
import com.bookmatch.BookMatch.entity.Book;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiService {

    private static final Pattern SCORE_PATTERN =
            Pattern.compile(
                    "SCORE:\\s*(\\d{1,3})",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern REASON_PATTERN =
            Pattern.compile(
                    "REASON:\\s*(.+)",
                    Pattern.CASE_INSENSITIVE
            );

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model,
            @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
            String baseUrl
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Gemini API key is missing."
            );
        }

        this.apiKey = apiKey;
        this.model = model;

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public AiMatchDto analyzeMatch(
            MovieDto movie,
            Book book
    ) {
        String prompt = buildPrompt(movie, book);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "maxOutputTokens", 150
                )
        );

        try {
            JsonNode response = restClient
                    .post()
                    .uri(
                            "/models/{model}:generateContent",
                            model
                    )
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            String generatedText =
                    extractGeneratedText(response);

            System.out.println(
                    "Gemini response for "
                            + book.getTitle()
                            + ": "
                            + generatedText
            );

            return parseResponse(
                    generatedText,
                    movie,
                    book
            );

        } catch (Exception exception) {
            System.err.println(
                    "========== GEMINI ERROR =========="
            );
            System.err.println(
                    "Book: " + book.getTitle()
            );
            System.err.println(
                    "Type: "
                            + exception.getClass().getName()
            );
            System.err.println(
                    "Message: "
                            + exception.getMessage()
            );
            System.err.println(
                    "=================================="
            );

            return fallbackMatch(movie, book);
        }
    }

    private String extractGeneratedText(JsonNode response) {

        if (response == null) {
            return null;
        }

        JsonNode textNode = response.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode()
                || textNode.isNull()) {
            return null;
        }

        return textNode.asText();
    }

    private String buildPrompt(
            MovieDto movie,
            Book book
    ) {
        return """
                Analyze how closely the following book matches the movie.

                Movie:
                Title: %s
                Genres: %s
                Description: %s

                Book:
                Title: %s
                Author: %s
                Genre: %s
                Tags: %s
                Description: %s

                Compare:
                - genre
                - themes
                - atmosphere
                - characters
                - storytelling style

                Return exactly two lines:

                SCORE: number between 0 and 100
                REASON: one short sentence

                Do not use Markdown.
                Do not add extra text.
                """
                .formatted(
                        safe(movie.title()),
                        movie.genres() == null
                                ? "Unknown"
                                : movie.genres(),
                        safe(movie.description()),
                        safe(book.getTitle()),
                        safe(book.getAuthor()),
                        safe(book.getGenre()),
                        safe(book.getTags()),
                        safe(book.getDescription())
                );
    }

    private AiMatchDto parseResponse(
            String text,
            MovieDto movie,
            Book book
    ) {
        if (text == null || text.isBlank()) {
            return fallbackMatch(movie, book);
        }

        Matcher scoreMatcher =
                SCORE_PATTERN.matcher(text);

        Matcher reasonMatcher =
                REASON_PATTERN.matcher(text);

        int score = 80;
        String reason =
                buildFallbackReason(movie, book);

        if (scoreMatcher.find()) {
            try {
                score = Integer.parseInt(
                        scoreMatcher.group(1)
                );

                score = Math.max(
                        0,
                        Math.min(100, score)
                );

            } catch (NumberFormatException ignored) {
                score = 80;
            }
        }

        if (reasonMatcher.find()) {
            String parsedReason =
                    reasonMatcher.group(1).trim();

            if (!parsedReason.isBlank()) {
                reason = parsedReason;
            }
        }

        return new AiMatchDto(
                score,
                reason
        );
    }

    private AiMatchDto fallbackMatch(
            MovieDto movie,
            Book book
    ) {
        return new AiMatchDto(
                80,
                buildFallbackReason(movie, book)
        );
    }

    private String buildFallbackReason(
            MovieDto movie,
            Book book
    ) {
        String movieTitle =
                movie == null
                        ? "the movie"
                        : safe(movie.title());

        String bookTitle =
                book == null
                        ? "The book"
                        : safe(book.getTitle());

        return bookTitle
                + " shares related themes and atmosphere with "
                + movieTitle
                + ".";
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        return value.trim();
    }
}