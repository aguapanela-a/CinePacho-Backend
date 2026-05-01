package CinePacho.demo.auth.dto;

import CinePacho.demo.shared.enumeration.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record AuthResponseDTO(

        @NotBlank(message = "No se ha generado el token correctamente")
        String token,

        @NotNull(message = "El tipo de usuario es obligatorio")
        UserType userType,

        @NotBlank(message = "El nombre de usuario no puede estar vacío")
        String name
) {
}
