package com.bookmatch.BookMatch.dto;

public record AiMatchDto(
        int matchScore,
        String matchReason
) {
}