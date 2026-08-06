package com.bookmatch.BookMatch.service;

import com.bookmatch.BookMatch.client.googlebooks.GoogleBooksClient;
import com.bookmatch.BookMatch.dto.AiMatchDto;
import com.bookmatch.BookMatch.dto.BookRecommendationDto;
import com.bookmatch.BookMatch.dto.MovieDto;
import com.bookmatch.BookMatch.dto.RecommendationResponse;
import com.bookmatch.BookMatch.entity.Book;
import com.bookmatch.BookMatch.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final BookRepository bookRepository;
    private final GoogleBooksClient googleBooksClient;
    private final MovieService movieService;
    private final GeminiService geminiService;

    /**
     * Returns movie data together with AI-analyzed book recommendations.
     */
    @Transactional
    public RecommendationResponse getFullRecommendation(String movie) {

        validateMovieTitle(movie);

        MovieDto movieData =
                movieService.searchMovie(movie.trim());

        List<Book> books =
                findAndEnrichBooks(movieData.title());

        List<BookRecommendationDto> recommendations =
                books.stream()
                        .map(book -> createAiRecommendation(
                                movieData,
                                book
                        ))
                        .toList();

        return new RecommendationResponse(
                movieData,
                recommendations
        );
    }

    /**
     * Existing endpoint that returns only Book entities.
     */
    @Transactional
    public List<Book> recommendBooks(String movie) {

        validateMovieTitle(movie);

        return findAndEnrichBooks(movie);
    }

    /**
     * Returns all books stored in PostgreSQL.
     */
    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * Converts one Book into a recommendation enriched by Gemini.
     */
    private BookRecommendationDto createAiRecommendation(
            MovieDto movie,
            Book book
    ) {
        try {
            AiMatchDto aiMatch =
                    geminiService.analyzeMatch(movie, book);

            return BookRecommendationDto.from(
                    book,
                    aiMatch
            );

        } catch (Exception exception) {
            System.err.println(
                    "Gemini analysis failed for "
                            + book.getTitle()
                            + ": "
                            + exception.getMessage()
            );

            AiMatchDto fallback = new AiMatchDto(
                    calculateFallbackScore(movie, book),
                    buildFallbackReason(movie, book)
            );

            return BookRecommendationDto.from(
                    book,
                    fallback
            );
        }
    }

    /**
     * Finds matching books and loads missing Google Books covers.
     */
    private List<Book> findAndEnrichBooks(String movie) {

        String keyword = mapMovieToKeyword(movie);

        List<Book> books =
                bookRepository.findByTagsContainingIgnoreCase(keyword);

        for (Book book : books) {
            enrichMissingCover(book);
        }

        return bookRepository.saveAll(books);
    }

    /**
     * Gets a Google Books cover only when it is not already stored.
     */
    private void enrichMissingCover(Book book) {

        boolean alreadyHasCover =
                book.getCoverUrl() != null
                        && !book.getCoverUrl().isBlank();

        if (alreadyHasCover) {
            return;
        }

        try {
            String coverUrl =
                    googleBooksClient.getBookCover(
                            book.getTitle(),
                            book.getAuthor()
                    );

            if (coverUrl != null && !coverUrl.isBlank()) {
                book.setCoverUrl(coverUrl);

                System.out.println(
                        "Cover saved for: "
                                + book.getTitle()
                );
            }

        } catch (Exception exception) {
            System.err.println(
                    "Could not load cover for "
                            + book.getTitle()
                            + ": "
                            + exception.getMessage()
            );
        }
    }

    /**
     * Local score used only if Gemini is unavailable.
     */
    private int calculateFallbackScore(
            MovieDto movie,
            Book book
    ) {
        int score = 60;

        String bookGenre =
                normalize(book.getGenre());

        String bookTags =
                normalize(book.getTags());

        boolean genreMatches = movie.genres() != null
                && movie.genres()
                .stream()
                .map(this::normalize)
                .anyMatch(genre ->
                        bookGenre.contains(genre)
                                || bookTags.contains(genre)
                );

        if (genreMatches) {
            score += 20;
        }

        String movieDescription =
                normalize(movie.description());

        if (book.getTags() != null) {
            String[] tags = book.getTags().split(",");

            for (String tag : tags) {
                String normalizedTag = normalize(tag);

                if (!normalizedTag.isBlank()
                        && movieDescription.contains(normalizedTag)) {
                    score += 5;
                }
            }
        }

        return Math.min(score, 95);
    }

    /**
     * Local reason used only if Gemini is unavailable.
     */
    private String buildFallbackReason(
            MovieDto movie,
            Book book
    ) {
        if (book.getTags() == null || book.getTags().isBlank()) {
            return book.getTitle()
                    + " shares a related genre and atmosphere with "
                    + movie.title()
                    + ".";
        }

        String mainTags = book.getTags()
                .replace(",", ", ");

        return book.getTitle()
                + " connects with "
                + movie.title()
                + " through themes such as "
                + mainTags
                + ".";
    }

    /**
     * Temporary mapping for the MVP recommendation database.
     */
    private String mapMovieToKeyword(String movie) {

        String normalizedMovie =
                normalize(movie);

        return switch (normalizedMovie) {
            case "interstellar" -> "space";

            case "inception" -> "mind";

            case "harry potter",
                 "harry potter and the philosopher's stone",
                 "harry potter and the sorcerer's stone" ->
                    "magic";

            case "the witcher",
                 "the witcher 3" ->
                    "fantasy";

            case "avatar" -> "nature";

            default -> normalizedMovie;
        };
    }

    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private void validateMovieTitle(String movie) {

        if (movie == null || movie.isBlank()) {
            throw new IllegalArgumentException(
                    "Movie title is required."
            );
        }
    }
}