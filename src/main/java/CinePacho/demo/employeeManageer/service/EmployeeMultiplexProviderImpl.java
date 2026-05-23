package CinePacho.demo.employeeManageer.service;

import CinePacho.demo.employeeManageer.entities.EmployeeEntity;
import CinePacho.demo.employeeManageer.repository.EmployeeRepository;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.auxiliaryClass.EmployeeMultiplexProvider;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmployeeMultiplexProviderImpl implements EmployeeMultiplexProvider {

    private final EmployeeRepository employeeRepository;

    public EmployeeMultiplexProviderImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UUID getMultiplexIdByUserEmail(String email) {
        // Encapsula la consulta del multiplex para mantener independencia entre módulos
        EmployeeEntity employee = employeeRepository.findByUser_Email(email);
        if (employee == null || employee.getMultiplex() == null) {
            throw new CinePachoException("El gerente no tiene multiplex asignado");
        }
        return employee.getMultiplex().getId();
    }
}
