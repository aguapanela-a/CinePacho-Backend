package CinePacho.demo.shared.registerData;

import CinePacho.demo.auth.dto.request.RegisterRequestDTO;
import CinePacho.demo.shared.enumeration.UserType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


//Se usa el campo "userType" del JSON como discriminador según el tipo de usuario se crea una o otra impl de la interface
// (en este caso solo BUYER porque es el único, por ahora, que se puede registrar po sí solo)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "userType")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = RegisterRequestDTO.class, name = "BUYER"),
        }
)
public interface RegisterData {
    String name();
    String email();
    String password();
    UserType userType();
}
