package CinePacho.demo.employeeManageer.factory;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.employeeManageer.dto.request.RegisterEmployeeRequestDTO;
import CinePacho.demo.employeeManageer.entities.EmployeeEntity;
import CinePacho.demo.employeeManageer.repository.EmployeeRepository;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.auxiliaryClass.MultiplexProvider;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.factory.UserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ManagerFactory implements UserFactory<RegisterEmployeeRequestDTO> {

    private final EmployeeRepository employeeRepository;
    private final MultiplexProvider multiplexProvider;

    @Autowired
    public ManagerFactory(EmployeeRepository employeeRepository, MultiplexProvider multiplexProvider) {
        this.employeeRepository = employeeRepository;
        this.multiplexProvider = multiplexProvider;
    }

    @Override
    public UserType getSupportedType() {
        return UserType.MANAGER; // Factory específica para gerentes
    }

    @Override
    public void createSpecificEntity(UserEntity user, RegisterEmployeeRequestDTO extraData) {

        EmployeeEntity manager = new EmployeeEntity();

        manager.setUser(user);
        if (extraData.multiplexId() == null) {
            // El gerente debe quedar asignado a un multiplex
            throw new CinePachoException("El multiplex es obligatorio para registrar un gerente");
        }
        manager.setMultiplex(multiplexProvider.getMultiplexById(extraData.multiplexId()));

        manager.setIdentityCard(extraData.indentityCard());
        manager.setPhoneNumber(extraData.phoneNumber());
        manager.setSalary(extraData.salary());
        manager.setRol(extraData.rol());
        manager.setUniqueCode(nextUniqueCode());

        employeeRepository.save(manager);
    }

    private long nextUniqueCode() {
        return employeeRepository.findTopByOrderByUniqueCodeDesc()
                .map(EmployeeEntity::getUniqueCode)
                .orElse(9999L) + 1;
    }

}
