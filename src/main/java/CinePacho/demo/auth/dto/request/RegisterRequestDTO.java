package CinePacho.demo.auth.dto.request;

import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.registerData.RegisterData;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        String email,

        @Size(min = 2, max = 30, message = "El nombre de usuario debe ser de 2 a 30 carácteres")
        @NotBlank(message = "El nombre no puede estar vacío")
        String name,

        @NotBlank(message = "La contraseña debe ser obligatoria")
        @Size(min = 8, message = "La contraseña debe ser de mínimo 8 carácteres")
        String password,

        @NotNull(message = "Debe incluir un tipo de usuario válido")
        UserType userType
) implements RegisterData {
}
