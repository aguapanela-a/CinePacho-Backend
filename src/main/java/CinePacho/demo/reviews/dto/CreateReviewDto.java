package CinePacho.demo.reviews.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateReviewDto(
        @NotBlank(message = "El id de la movie es requerido")
        Long movieId,
        Integer rating,
        String comment)
{ }