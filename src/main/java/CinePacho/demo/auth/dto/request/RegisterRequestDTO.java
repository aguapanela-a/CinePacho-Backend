package CinePacho.demo.auth.dto.request;

import CinePacho.demo.shared.enumeration.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        String email,

        @NotBlank(message = "El nombre no puede estar vacío")
        String name,

        @NotBlank(message = "La contraseña debe ser obligatoria")
        @Size(min = 8, message = "La contraseña debe ser de mínimo 8 carácteres")
        String password,

        @NotNull(message = "Debe incluir su tipo de usuario")
        UserType userType
) {
}
