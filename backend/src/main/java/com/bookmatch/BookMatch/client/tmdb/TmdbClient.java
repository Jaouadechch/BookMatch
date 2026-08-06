package com.bookmatch.BookMatch.client.tmdb;

import com.bookmatch.BookMatch.dto.MovieDto;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Component
public class TmdbClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String imageBaseUrl;
    private final String token;

    public TmdbClient(
            ObjectMapper objectMapper,
            @Value("${tmdb.base-url}") String baseUrl,
            @Value("${tmdb.image-base-url}") String imageBaseUrl,
            @Value("${tmdb.token}") String token
    ) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.imageBaseUrl = imageBaseUrl;
        this.token = token;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public MovieDto searchMovie(String query) {
        validateConfiguration();

        URI searchUri = UriComponentsBuilder
                .fromUriString(baseUrl + "/search/movie")
                .queryParam("query", query)
                .queryParam("include_adult", false)
                .queryParam("language", "en-US")
                .queryParam("page", 1)
                .build()
                .encode()
                .toUri();

        TmdbSearchResponse searchResponse =
                sendRequest(searchUri, TmdbSearchResponse.class);

        if (searchResponse.results() == null
                || searchResponse.results().isEmpty()) {
            throw new IllegalArgumentException(
                    "Movie not found: " + query
            );
        }

        Long movieId = searchResponse.results().get(0).id();

        return getMovieDetails(movieId);
    }

    private MovieDto getMovieDetails(Long movieId) {
        URI detailsUri = UriComponentsBuilder
                .fromUriString(baseUrl + "/movie/{movieId}")
                .queryParam("language", "en-US")
                .buildAndExpand(movieId)
                .encode()
                .toUri();

        TmdbMovieDetails details =
                sendRequest(detailsUri, TmdbMovieDetails.class);

        List<String> genres = details.genres() == null
                ? List.of()
                : details.genres()
                .stream()
                .map(TmdbGenre::name)
                .toList();

        return new MovieDto(
                details.id(),
                details.title(),
                extractYear(details.releaseDate()),
                "movie",
                createPosterUrl(details.posterPath()),
                genres,
                details.overview()
        );
    }

    private <T> T sendRequest(
            URI uri,
            Class<T> responseType
    ) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "TMDB request failed with status "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    responseType
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "TMDB request was interrupted",
                    exception
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read TMDB response",
                    exception
            );
        }
    }

    private Integer extractYear(String releaseDate) {
        if (releaseDate == null || releaseDate.length() < 4) {
            return null;
        }

        try {
            return Integer.parseInt(
                    releaseDate.substring(0, 4)
            );
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String createPosterUrl(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return null;
        }

        return imageBaseUrl + posterPath;
    }

    private void validateConfiguration() {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "TMDB_TOKEN is not configured"
            );
        }
    }
}