package CinePacho.demo.movie.dto;

import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.shared.tmdbGenre.TmdbGenreMapper;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ScreeningResponseDTO(

        @NotBlank(message = "La función debe tener una fecha")
        LocalDateTime dateTime,

        @NotBlank(message = "La función debe tener una precio")
        BigDecimal price,

        @NotBlank(message = "La función debe tener un idioma")
        String originalLanguage,

        @NotBlank(message = "La función debe tener un título")
        String originalTitle,

        @NotBlank(message = "La función debe tener una descripción")
        String overview,

        Double rating,

        String director,

        @NotBlank(message = "La función debe tener lista de géneros")
        List<String> genres
) {
}
