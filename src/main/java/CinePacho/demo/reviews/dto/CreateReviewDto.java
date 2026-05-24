package CinePacho.demo.reviews.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;

public record CreateReviewDto(
        @NotBlank(message = "El id de la movie es requerido")
        Long movieId,
        @Range(min = 1, max = 5, message = "El rating debe estar entre 1 y 5")
        Integer rating,
        @Size(min = 0, max = 255, message = "El comentario debe tener entre 0 y 255 caracteres")
        String comment)
{ }