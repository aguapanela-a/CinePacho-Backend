package CinePacho.demo.shared.serviceSecurity;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.employeeManageer.repository.EmployeeRepository;
import CinePacho.demo.shared.auxiliaryClass.EmployeeMultiplexProvider;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.enumeration.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessValidator {

    private final EmployeeMultiplexProvider employeeMultiplexProvider;
    private final EmployeeRepository employeeRepository;
    private static final int ROLE_UPDATE_COOLDOWN_MONTHS = 3;

    public void validateMultiplexAccess(UUID multiplexId) {
        // Regla central para restringir el alcance del gerente
        UserEntity user = getCurrentUser();
        if (user.getUserType() == UserType.ADMIN) {
            return; // El admin tiene acceso global
        }
        if (user.getUserType() != UserType.MANAGER) {
            throw new CinePachoException("No tienes permisos para gestionar el multiplex");
        }

        UUID managerMultiplexId = getManagerMultiplexId(user);
        if (!managerMultiplexId.equals(multiplexId)) {
            throw new CinePachoException("No puedes gestionar otro multiplex");
        }
    }

    public UUID getScopedMultiplexIdForAdminOrManager() {
        // Permite filtrar listados cuando el usuario es gerente
        UserEntity user = getCurrentUser();
        if (user.getUserType() == UserType.ADMIN) {
            return null;
        }
        if (user.getUserType() != UserType.MANAGER) {
            throw new CinePachoException("No tienes permisos para consultar multiplex");
        }
        return getManagerMultiplexId(user);
    }

    public void validateEmployeeRegistrationAccess(UserType requestedUserType, UUID multiplexId) {
        // Centraliza las reglas para registrar personal sin acoplar employeeManageer con auth/security.
        UserEntity user = getCurrentUser();
        if (user.getUserType() == UserType.ADMIN) {
            return; // El admin puede registrar empleados y gerentes en cualquier multiplex.
        }
        if (user.getUserType() != UserType.MANAGER) {
            throw new CinePachoException("No tienes permisos para registrar personal");
        }
        if (requestedUserType == UserType.MANAGER) {
            throw new CinePachoException("No tienes permisos para crear gerentes");
        }

        UUID managerMultiplexId = getManagerMultiplexId(user);
        if (!managerMultiplexId.equals(multiplexId)) {
            throw new CinePachoException("No puedes registrar personal en otro multiplex");
        }
    }

    private UUID getManagerMultiplexId(UserEntity user) {
        return employeeMultiplexProvider.getMultiplexIdByUserEmail(user.getEmail());
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserEntity)) {
            throw new CinePachoException("No hay usuario autenticado");
        }
        return (UserEntity) authentication.getPrincipal();
    }

    public void validateEmployeeUpdateFrequency(String employeeEmail) {
        // Implementa la lógica para validar que el cargo y rol de un empleado solo pueda ser cambiado cada 3 meses.
        // Cambio: se consulta la fecha del último cambio en BD y se compara con la fecha actual.
        if (employeeEmail == null || employeeEmail.trim().isEmpty()) {
            throw new CinePachoException("El email del empleado es requerido");
        }

        String normalizedEmail = employeeEmail.trim().toLowerCase();
        var employee = employeeRepository.findByUser_Email(normalizedEmail);
        if (employee == null) {
            throw new CinePachoException("No se encontró el empleado con el email proporcionado");
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime lastUpdate = employee.getRoleUpdatedAt();

        if (lastUpdate != null) {
            LocalDateTime nextAllowed = lastUpdate.plusMonths(ROLE_UPDATE_COOLDOWN_MONTHS);
            if (now.isBefore(nextAllowed)) {
                throw new CinePachoException("No puedes actualizar cargo/rol del empleado antes de 3 meses");
            }
        }
    }

}
