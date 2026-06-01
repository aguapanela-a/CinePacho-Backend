package CinePacho.demo.shared.auxiliaryClass;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter@Setter
@NoArgsConstructor
public class DTOResponse {
    private String message;
    private String status;

    // Helper para construir respuestas estándar con el código HTTP en el campo status
    public static DTOResponse withStatus(String message, int statusCode) {
        DTOResponse response = new DTOResponse();
        response.setMessage(message);
        response.setStatus(String.valueOf(statusCode));
        return response;
    }
}
