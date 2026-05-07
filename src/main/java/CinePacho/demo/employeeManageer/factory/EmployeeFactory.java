package CinePacho.demo.employeeManageer.factory;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.employeeManageer.dto.request.RegisterEmployeeRequestDTO;
import CinePacho.demo.employeeManageer.entities.EmployeeEntity;
import CinePacho.demo.employeeManageer.repository.EmployeeRepository;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.factory.UserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmployeeFactory implements UserFactory<RegisterEmployeeRequestDTO> {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeFactory(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserType getSupportedType(){
        return UserType.EMPLOYEE;
    }

    @Override
    public void createSpecificEntity(UserEntity user, RegisterEmployeeRequestDTO extraData){

        EmployeeEntity employee = new EmployeeEntity();

        employee.setUser(user);

        employee.setIdentityCard(extraData.indentityCard());
        employee.setPhoneNumber(extraData.phoneNumber());
        employee.setSalary(extraData.salary());
        employee.setPosition(extraData.position());
        employee.setUniqueCode(nextUniqueCode());


        employeeRepository.save(employee);
    }

    private long nextUniqueCode() {
        return employeeRepository.findTopByOrderByUniqueCodeDesc()
                .map(EmployeeEntity::getUniqueCode)
                .orElse(9999L) + 1;
    }

}
