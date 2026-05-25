package CinePacho.demo.reviews.dto;

import CinePacho.demo.reviews.enumeration.ReviewType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewDetailResponseDto(
        UUID reviewId,
        Long movieId,
        ReviewType reviewType,
        String comment,
        Integer rating,
        LocalDateTime createdAt,
        String movieTitle
) {
}
