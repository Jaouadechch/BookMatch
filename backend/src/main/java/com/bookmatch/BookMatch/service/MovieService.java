package com.bookmatch.BookMatch.service;

import com.bookmatch.BookMatch.client.tmdb.TmdbClient;
import com.bookmatch.BookMatch.dto.MovieDto;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

    private final TmdbClient tmdbClient;

    public MovieService(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    public MovieDto searchMovie(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Movie title is required");
        }

        return tmdbClient.searchMovie(query.trim());
    }
}