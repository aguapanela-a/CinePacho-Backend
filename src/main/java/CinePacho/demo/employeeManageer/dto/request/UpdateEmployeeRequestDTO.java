package CinePacho.demo.employeeManageer.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import CinePacho.demo.employeeManageer.enumeration.RolEmployee;
import CinePacho.demo.shared.enumeration.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeRequestDTO(
    String uniqueCode, // por si lo necesitas
    @NotBlank(message = "El email es obligatorio")
    String email,
    @NotBlank(message = "El nombre es obligatorio")
    String name,

    // AQUÍ: Permite vacío para la edición
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    String password,

    @NotNull UserType userType,
    @NotBlank String indentityCard,
    @NotBlank String phoneNumber,
    @NotNull BigDecimal salary, // o el tipo que uses
    @NotNull RolEmployee rol,   // o tu enum de Rol
    
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull LocalDateTime startDate,
    
    @NotNull UUID multiplexId
) {}