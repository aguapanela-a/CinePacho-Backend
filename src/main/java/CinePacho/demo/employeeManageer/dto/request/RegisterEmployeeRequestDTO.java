package CinePacho.demo.employeeManageer.dto.request;

import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.registerData.RegisterData;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

public record RegisterEmployeeRequestDTO (

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
        UserType userType,

        @NotBlank(message = "La cédula deciudadanía es obligatoria")
        @Size(min = 8, max = 10, message = "La cedula de ciudadanía debe tener entre 8 y 10 dígitos")
        String indentityCard,

        @NotBlank(message = "El número tenefónico es obligatorio")
        @Size(min = 10, max = 10, message = "Su nuimeo de celular  tener 10 dígitos")
        String phoneNumber,

        @PositiveOrZero(message = "El salario debe ser positivo")
        BigDecimal salary,

        @NotNull(message = "El rol del empleado es obligatorio y debe ser válido")
        String position
)
implements RegisterData {}
