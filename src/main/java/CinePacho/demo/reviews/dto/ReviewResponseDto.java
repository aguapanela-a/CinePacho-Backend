package CinePacho.demo.reviews.dto;

import CinePacho.demo.reviews.enumeration.ReviewType;

public record ReviewResponseDto(
        ReviewType reviewType,
        String message,
        Integer rating
) {
}
