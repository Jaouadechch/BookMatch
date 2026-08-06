package com.bookmatch.BookMatch.dto;

import java.util.List;

public record MovieDto(
        Long id,
        String title,
        Integer year,
        String type,
        String posterUrl,
        List<String> genres,
        String description
) {
}