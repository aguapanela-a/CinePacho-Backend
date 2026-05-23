package CinePacho.demo.shared.auxiliaryClass;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface EmployeeMultiplexProvider {
    UUID getMultiplexIdByUserEmail(String email); // Consulta el multiplex asignado al personal
}
