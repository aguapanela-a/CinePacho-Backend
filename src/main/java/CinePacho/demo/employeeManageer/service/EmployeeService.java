package CinePacho.demo.employeeManageer.service;

import CinePacho.demo.auth.dto.response.RegisterResponseDTO;
import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.employeeManageer.dto.request.RegisterEmployeeRequestDTO;
import CinePacho.demo.employeeManageer.entities.EmployeeEntity;
import CinePacho.demo.employeeManageer.repository.EmployeeRepository;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.auxiliaryClass.MultiplexProvider;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.shared.serviceSecurity.AccessValidator;
import CinePacho.demo.shared.user.UserCreationService;
import CinePacho.demo.shared.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class EmployeeService {
    private final UserCreationService userCreationService;
    private final UserRepository userRepository;
    private final AccessValidator accessValidator;
    private final EmployeeRepository employeeRepository;
    private final MultiplexProvider multiplexProvider;

    @Autowired
    public EmployeeService(UserCreationService userCreationService,
                           UserRepository userRepository,
                           AccessValidator accessValidator,
                           EmployeeRepository employeeRepository,
                           MultiplexProvider multiplexProvider) {
        this.userCreationService = userCreationService;
        this.userRepository = userRepository;
        this.accessValidator = accessValidator;
        this.employeeRepository = employeeRepository;
        this.multiplexProvider = multiplexProvider;
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

    @Transactional
    public RegisterResponseDTO updateEmployee(RegisterEmployeeRequestDTO registerEmployeeRequestDTO) {
        if (registerEmployeeRequestDTO.userType() != UserType.EMPLOYEE
                && registerEmployeeRequestDTO.userType() != UserType.MANAGER) {
            // Se limita la actualización de personal a empleado o gerente.
            throw new CinePachoException("El tipo de usuario no es valido para este registro");
        }

        // Valida rol y alcance: MANAGER solo puede actualizar EMPLOYEE en su multiplex.
        accessValidator.validateEmployeeRegistrationAccess(
                registerEmployeeRequestDTO.userType(),
                registerEmployeeRequestDTO.multiplexId()
        );

        UserEntity user = userRepository.findByEmail(registerEmployeeRequestDTO.email())
                .orElseThrow(() -> new CinePachoException("No se encontró un usuario con el email proporcionado"));
        EmployeeEntity employee = employeeRepository.findByUser_Email(registerEmployeeRequestDTO.email());
        if (employee == null) {
            throw new CinePachoException("No se encontró el empleado asociado al email proporcionado");
        }

        boolean userTypeChanged = user.getUserType() != registerEmployeeRequestDTO.userType();
        boolean roleChanged = employee.getRol() != registerEmployeeRequestDTO.rol();
        if (userTypeChanged || roleChanged) {
            // Valida que el cargo/rol solo se pueda cambiar cada 3 meses
            accessValidator.validateEmployeeUpdateFrequency(registerEmployeeRequestDTO.email());
            // Se registra la fecha del cambio real de cargo/rol
            employee.setRoleUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        }

        // Actualiza campos del usuario y entidad concreta de empleado o gerente.
        userCreationService.updateUser(
                user,
                registerEmployeeRequestDTO.name(),
                registerEmployeeRequestDTO.password(),
                registerEmployeeRequestDTO.userType(),
                registerEmployeeRequestDTO.email(),
                registerEmployeeRequestDTO
        );

        // Actualiza datos del empleado
        employee.setIdentityCard(registerEmployeeRequestDTO.indentityCard());
        employee.setPhoneNumber(registerEmployeeRequestDTO.phoneNumber());
        employee.setSalary(registerEmployeeRequestDTO.salary());
        employee.setRol(registerEmployeeRequestDTO.rol());
        employee.setMultiplex(multiplexProvider.getMultiplexById(registerEmployeeRequestDTO.multiplexId()));

        userRepository.save(user);
        employeeRepository.save(employee);

        return new RegisterResponseDTO(user.getUserType(), user.getUsername(), "Se ha actualizado correctamente el empleado");
    }
}
