package CinePacho.demo.movie.dto;

import CinePacho.demo.movie.entities.MovieScreening;
import jakarta.validation.constraints.NotBlank;

public record ScreeningResponseDTO() {
    @NotBlank
    static MovieScreening movieScreening;
}
