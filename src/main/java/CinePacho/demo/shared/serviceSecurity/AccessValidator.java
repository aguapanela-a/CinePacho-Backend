package CinePacho.demo.shared.serviceSecurity;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.shared.auxiliaryClass.EmployeeMultiplexProvider;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.enumeration.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessValidator {

    private final EmployeeMultiplexProvider employeeMultiplexProvider;

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
}
