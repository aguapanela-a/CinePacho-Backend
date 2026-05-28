package CinePacho.demo.movie.dto.response;

import CinePacho.demo.movie.enumeration.ScreeningStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ScreeningResponseDTO(
        @NotBlank
        UUID screeningId,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @NotBlank(message = "La función debe tener una fecha")
        LocalDateTime dateTime,

        @NotBlank(message = "La función debe tener un idioma")
        String originalLanguage,

        @NotBlank(message = "La función debe tener un título")
        String originalTitle,

        @NotBlank(message = "La función debe tener una descripción")
        String overview,

        Double rating,

        String director,

        @NotBlank
        ScreeningStatus status,

        @NotBlank(message = "La función debe tener lista de géneros")
        List<String> genres
) {
}
