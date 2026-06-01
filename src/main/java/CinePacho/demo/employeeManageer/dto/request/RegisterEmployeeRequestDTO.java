package CinePacho.demo.employeeManageer.dto.request;

import CinePacho.demo.employeeManageer.enumeration.RolEmployee;
import CinePacho.demo.shared.enumeration.UserType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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

        @NotBlank(message = "La cédula de ciudadanía es obligatoria")
        @Size(min = 8, max = 10, message = "La cedula de ciudadanía debe tener entre 8 y 10 dígitos")
        String indentityCard,

        @NotBlank(message = "El número tenefónico es obligatorio")
        @Size(min = 10, max = 10, message = "Su nuimeo de celular  tener 10 dígitos")
        String phoneNumber,

        @PositiveOrZero(message = "El salario debe ser positivo")
        BigDecimal salary,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDateTime startDate,

        @NotNull(message = "El rol del empleado es obligatorio y debe ser válido")
        RolEmployee rol,

        @NotNull(message = "El multiplex es obligatorio")
        UUID multiplexId // Multiplex asignado al empleado/gerente
){}
