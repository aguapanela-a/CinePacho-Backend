package CinePacho.demo.auth.dto;

import CinePacho.demo.shared.enumeration.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDTO(
        @NotBlank(message = "Debe ingresar un email válido")
        String email,

        @NotBlank(message = "El nombre no puede estar vacío")
        String name,

        @NotBlank(message = "La contraseña debe ser de mínimo 8 carácteres")
        String password,

        @NotNull(message = "Debe incluir su tipo de usuario")
        UserType userType
) {
}
