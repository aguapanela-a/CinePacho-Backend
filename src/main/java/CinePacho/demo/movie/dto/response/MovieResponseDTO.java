package CinePacho.demo.movie.dto.response;

import jakarta.validation.constraints.NotBlank;

public record MovieResponseDTO(
        @NotBlank (message = "Debes enviar el título de la peli al front")
        String originarTitle,

        @NotBlank(message = "El mensaje de confirmación es obligatorio")
        String message
) {
}
