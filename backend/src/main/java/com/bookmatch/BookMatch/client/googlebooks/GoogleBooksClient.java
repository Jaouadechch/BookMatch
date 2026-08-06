package com.bookmatch.BookMatch.client.googlebooks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class GoogleBooksClient {

    private static final String GOOGLE_BOOKS_URL =
            "https://www.googleapis.com/books/v1/volumes";

    private final RestClient restClient;
    private final String apiKey;

    public GoogleBooksClient(
            @Value("${google.books.api-key}") String apiKey
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Google Books API key is missing."
            );
        }

        this.apiKey = apiKey;
        this.restClient = RestClient.create();
    }

    public String getBookCover(
            String title,
            String author
    ) {
        if (title == null || title.isBlank()) {
            return null;
        }

        // First search: exact title and author.
        String coverUrl = searchCover(title, author);

        // Fallback: title only.
        if (coverUrl == null
                && author != null
                && !author.isBlank()) {

            coverUrl = searchCover(title, null);
        }

        return coverUrl;
    }

    private String searchCover(
            String title,
            String author
    ) {
        String query =
                "intitle:\"" + title.trim() + "\"";

        if (author != null && !author.isBlank()) {
            query += " inauthor:\"" + author.trim() + "\"";
        }

        URI uri = UriComponentsBuilder
                .fromUriString(GOOGLE_BOOKS_URL)
                .queryParam("q", query)
                .queryParam("printType", "books")
                .queryParam("orderBy", "relevance")
                .queryParam("maxResults", 10)
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();

        try {
            GoogleBooksResponse response = restClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(GoogleBooksResponse.class);

            if (response == null
                    || response.getItems() == null
                    || response.getItems().isEmpty()) {

                System.out.println(
                        "No Google Books results for: " + title
                );

                return null;
            }

            for (GoogleBookItem item : response.getItems()) {

                if (item == null
                        || item.getVolumeInfo() == null
                        || item.getVolumeInfo()
                        .getImageLinks() == null) {
                    continue;
                }

                GoogleBookImageLinks imageLinks =
                        item.getVolumeInfo().getImageLinks();

                String coverUrl = imageLinks.getThumbnail();

                if (coverUrl == null || coverUrl.isBlank()) {
                    coverUrl = imageLinks.getSmallThumbnail();
                }

                if (coverUrl != null && !coverUrl.isBlank()) {
                    return normalizeCoverUrl(coverUrl);
                }
            }

            System.out.println(
                    "Books found but no cover available for: "
                            + title
            );

        } catch (Exception exception) {
            System.err.println(
                    "Google Books request failed for "
                            + title
                            + ": "
                            + exception.getMessage()
            );
        }

        return null;
    }

    private String normalizeCoverUrl(String coverUrl) {
        return coverUrl.replace("http://", "https://");
    }
}