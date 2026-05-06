package CinePacho.demo.auth.dto.response;

import CinePacho.demo.shared.enumeration.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AuthResponseDTO {
    
    @NotBlank(message = "No se ha generado el token correctamente")
    String token;

    @NotNull(message = "El tipo de usuario es obligatorio")
    UserType userType;

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    String name;

}
