package com.bookmatch.BookMatch.client.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbGenre(
        Long id,
        String name
) {
}