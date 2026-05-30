package CinePacho.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidTypeIdException;

@RestControllerAdvice

public class ExceptionController {

    //Errores de negocio
    @ExceptionHandler(CinePachoException.class)
    public ResponseEntity<ErrorDTO> cinePachoException(Exception ex){
        ErrorDTO error = new ErrorDTO(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    //Errores 403 forbidden
    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public ResponseEntity<ErrorDTO> cinePachoException(HttpClientErrorException.Forbidden ex){
        ErrorDTO error = new ErrorDTO(HttpStatus.FORBIDDEN.value(), "La solicitud falló por permisos insuficientes: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    //Validar tipos de usuario de entrada al registro
    @ExceptionHandler(InvalidTypeIdException.class)
    public ResponseEntity<ErrorDTO> handleInvalidUserType(InvalidTypeIdException ex) {
       ErrorDTO error = new ErrorDTO(HttpStatus.BAD_REQUEST.value(), "Tipo de usuario no válido para auto-registro. " + ex.getMessage());
       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Atrapa el error cuando Spring no puede convertir el String al Enum
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDTO> handleEnumMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorDTO error = new ErrorDTO(HttpStatus.BAD_REQUEST.value(),"Valor inválido para el parámetro: " + ex.getName() +
                ". Por favor, envíe un parámetro válido." + ex.getMessage() ) ;

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    //Errores de servidor
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> exception(Exception ex){
        ErrorDTO error = new ErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Ocurrió un error interno en el servidor. Inténtalo más tarde." + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
