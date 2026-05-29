package CinePacho.demo.employeeManageer.service;

import CinePacho.demo.auth.dto.response.RegisterResponseDTO;
import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.employeeManageer.dto.request.RegisterEmployeeRequestDTO;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.serviceSecurity.AccessValidator;
import CinePacho.demo.shared.user.UserCreationService;
import CinePacho.demo.shared.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {
    private final UserCreationService userCreationService;
    private final UserRepository userRepository;
    private final AccessValidator accessValidator;

    @Autowired
    public EmployeeService(UserCreationService userCreationService, UserRepository userRepository, AccessValidator accessValidator) {
        this.userCreationService = userCreationService;
        this.userRepository = userRepository;
        this.accessValidator = accessValidator;
    }

    @Transactional
    public RegisterResponseDTO registerEmployee(RegisterEmployeeRequestDTO registerEmployeeRequestDTO) {
        if (registerEmployeeRequestDTO.userType() != UserType.EMPLOYEE
                && registerEmployeeRequestDTO.userType() != UserType.MANAGER) {
            // Se limita el registro de personal a empleado o gerente.
            throw new CinePachoException("El tipo de usuario no es valido para este registro");
        }

        // Valida rol y alcance: MANAGER solo puede crear EMPLOYEE en su multiplex.
        accessValidator.validateEmployeeRegistrationAccess(
                registerEmployeeRequestDTO.userType(),
                registerEmployeeRequestDTO.multiplexId()
        );

        // Creacion de UserEntity y entidad concreta de empleado o gerente.
        UserEntity user = userCreationService.createUser(
                registerEmployeeRequestDTO.name(),
                registerEmployeeRequestDTO.password(),
                registerEmployeeRequestDTO.userType(),
                registerEmployeeRequestDTO.email(),
                registerEmployeeRequestDTO
        );

        user.setEnabled(true);
        userRepository.save(user);

        return new RegisterResponseDTO(user.getUserType(), user.getUsername(), "Se ha creado correctamente el empleado");
    }
}
