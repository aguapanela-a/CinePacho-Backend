package CinePacho.demo.employeeManageer.factory;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.employeeManageer.dto.request.RegisterEmployeeRequestDTO;
import CinePacho.demo.employeeManageer.entities.EmployeeEntity;
import CinePacho.demo.employeeManageer.repository.EmployeeRepository;
import CinePacho.demo.shared.auxiliaryClass.MultiplexProvider;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.factory.UserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class EmployeeFactory implements UserFactory<RegisterEmployeeRequestDTO> {

    private final EmployeeRepository employeeRepository;
    private final MultiplexProvider multiplexProvider;

    @Autowired
    public EmployeeFactory(EmployeeRepository employeeRepository, MultiplexProvider multiplexProvider) {
        this.employeeRepository = employeeRepository;
        this.multiplexProvider = multiplexProvider;
    }

    @Override
    public UserType getSupportedType(){
        return UserType.EMPLOYEE;
    }

    @Override
    public void createSpecificEntity(UserEntity user, RegisterEmployeeRequestDTO extraData){

        EmployeeEntity employee = new EmployeeEntity();

        employee.setUser(user);
        if (extraData.multiplexId() == null) {
            // Validación central para evitar empleados sin multiplex asignado
            throw new CinePachoException("El multiplex es obligatorio para registrar personal");
        }
        employee.setMultiplex(multiplexProvider.getMultiplexById(extraData.multiplexId()));
        // Asignar nombre completo del empleado al username del usuario
        user.setUsername(extraData.name());

        employee.setIdentityCard(extraData.indentityCard());
        employee.setPhoneNumber(extraData.phoneNumber());
        employee.setSalary(extraData.salary());
        employee.setRol(extraData.rol());
        employee.setUniqueCode(nextUniqueCode());
        employee.setStartDate(extraData.startDate());
        // Se registra la fecha del último cambio de cargo/rol al momento de crear el empleado
        employee.setRoleUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));


        employeeRepository.save(employee);
    }

    private long nextUniqueCode() {
        return employeeRepository.findTopByOrderByUniqueCodeDesc()
                .map(EmployeeEntity::getUniqueCode)
                .orElse(9999L) + 1;
    }

}
