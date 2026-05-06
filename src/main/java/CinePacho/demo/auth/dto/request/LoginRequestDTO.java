package CinePacho.demo.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record LoginRequestDTO(

        @NotBlank(message = "El correo es obligatorio")
        @Email
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
