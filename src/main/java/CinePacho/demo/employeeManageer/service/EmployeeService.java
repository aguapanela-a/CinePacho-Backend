package CinePacho.demo.employeeManageer.service;

import CinePacho.demo.auth.dto.response.RegisterResponseDTO;
import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.employeeManageer.dto.request.RegisterEmployeeRequestDTO;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.user.UserCreationService;
import CinePacho.demo.shared.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {
    private UserCreationService userCreationService;
    private UserRepository userRepository;

    @Autowired
    public EmployeeService(UserCreationService userCreationService,  UserRepository userRepository) {
        this.userCreationService = userCreationService;
        this.userRepository = userRepository;
    }

    @Transactional
    public RegisterResponseDTO registerEmployee(RegisterEmployeeRequestDTO registerEmployeeRequestDTO){
        //Creación de UserEntity y entidad concreta de empleado
        UserEntity user = userCreationService.createUser(
                registerEmployeeRequestDTO.name(),
                registerEmployeeRequestDTO.password(),
                UserType.EMPLOYEE,
                registerEmployeeRequestDTO.email(),
                registerEmployeeRequestDTO
        );

        user.setEnabled(true);
        userRepository.save(user);

        return new RegisterResponseDTO(user.getUserType(),user.getUsername(), "Se ha creado correctamente el empleado");
    }
}
