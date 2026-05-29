package CinePacho.demo.movie.dto.response;

import jakarta.validation.constraints.NotBlank;

public record MovieResponseDTO(
        @NotBlank (message = "Debes enviar el título de la peli al front")
        String originarTitle,

        @NotBlank(message = "Debes enviar el director de la peli al front")
        String director,

        @NotBlank(message = "El mensaje de confirmación es obligatorio")
        String message
) {
}
