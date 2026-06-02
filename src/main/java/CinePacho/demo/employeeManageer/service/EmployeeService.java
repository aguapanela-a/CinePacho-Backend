package CinePacho.demo.employeeManageer.service;

import CinePacho.demo.auth.dto.response.RegisterResponseDTO;
import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.employeeManageer.dto.request.RegisterEmployeeRequestDTO;
import CinePacho.demo.employeeManageer.dto.request.UpdateEmployeeRequestDTO;
import CinePacho.demo.employeeManageer.dto.response.EmployeesResponseDTO;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public List<EmployeesResponseDTO> getAll(){
        List<EmployeeEntity> employees = employeeRepository.findAll();

        // Implementación pendiente: se debe obtener la lista de empleados y mapearla a EmployeesResponseDTO
        return employees.stream().map(employee -> new EmployeesResponseDTO(
                employee.getUser().getName(),
                employee.getUser().getEmail(),
                employee.getPhoneNumber(),
                employee.getRol().toString(),
                employee.getIdentityCard(),
                employee.getSalary(),
                employee.getUniqueCode(),
                employee.getMultiplex().getName(),
                employee.getStartDate() != null ? employee.getStartDate().toLocalDate() : null,
                employee.getRoleUpdatedAt() != null ? employee.getRoleUpdatedAt().toLocalDate() : null
        )).collect(Collectors.toList());
    }


    public List<EmployeesResponseDTO> getAllEmployeesByMultiplex(UUID multiplexId) {

        List<EmployeeEntity> employees = employeeRepository.findAllByMultiplex_Id(multiplexId);

        employees.forEach(employee -> {
            System.out.println(employee.getUser().getName());

        });
        // Implementación pendiente: se debe obtener la lista de empleados y mapearla a EmployeesResponseDTO
        return employees.stream().map(employee -> new EmployeesResponseDTO(
                employee.getUser().getName(),
                employee.getUser().getEmail(),
                employee.getPhoneNumber(),
                employee.getRol().toString(),
                employee.getIdentityCard(),
                employee.getSalary(),
                employee.getUniqueCode(),
                employee.getMultiplex().getName(),
                employee.getStartDate() != null ? employee.getStartDate().toLocalDate() : null,
                employee.getRoleUpdatedAt() != null ? employee.getRoleUpdatedAt().toLocalDate() : null
        )).collect(Collectors.toList());


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
public RegisterResponseDTO updateEmployee(UpdateEmployeeRequestDTO updateEmployeeRequestDTO) {
    if (updateEmployeeRequestDTO.userType() != UserType.EMPLOYEE
            && updateEmployeeRequestDTO.userType() != UserType.MANAGER) {
        throw new CinePachoException("El tipo de usuario no es valido para este registro");
    }

    // Valida rol y alcance
    accessValidator.validateEmployeeRegistrationAccess(
            updateEmployeeRequestDTO.userType(),
            updateEmployeeRequestDTO.multiplexId()
    );

    UserEntity user = userRepository.findByEmail(updateEmployeeRequestDTO.email())
            .orElseThrow(() -> new CinePachoException("No se encontró un usuario con el email proporcionado"));
    EmployeeEntity employee = employeeRepository.findByUser_Email(updateEmployeeRequestDTO.email());
    if (employee == null) {
        throw new CinePachoException("No se encontró el empleado asociado al email proporcionado");
    }

    boolean userTypeChanged = user.getUserType() != updateEmployeeRequestDTO.userType();
    boolean roleChanged = employee.getRol() != updateEmployeeRequestDTO.rol();
    if (userTypeChanged || roleChanged) {
        accessValidator.validateEmployeeUpdateFrequency(updateEmployeeRequestDTO.email());
        employee.setRoleUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
    }

    // --- NUEVA LÓGICA PARA LA CONTRASEÑA ---
    // Verificamos que no sea nula, que no esté vacía y que no sean solo espacios en blanco
    if (updateEmployeeRequestDTO.password() != null && !updateEmployeeRequestDTO.password().trim().isEmpty()) {
        // Opción A: Si tu userCreationService.updateUser ya maneja la contraseña, descoméntala allá.
        // Opción B: Si usas Spring Security, encríptala directamente aquí antes de guardar:
        // String encryptedPassword = passwordEncoder.encode(updateEmployeeRequestDTO.password());
        // user.setPassword(encryptedPassword);
        
        // Nota: Si vas a usar la opción de abajo (Paso 2), simplemente descomenta la línea en el servicio:
    }

    // Actualiza campos del usuario utilizando tu servicio existente
    userCreationService.updateUser(
            user,
            updateEmployeeRequestDTO.name(),
            updateEmployeeRequestDTO.userType(),
            updateEmployeeRequestDTO.email(),
            updateEmployeeRequestDTO
    );

    // Actualiza datos del empleado
    employee.setIdentityCard(updateEmployeeRequestDTO.indentityCard());
    employee.setPhoneNumber(updateEmployeeRequestDTO.phoneNumber());
    employee.setSalary(updateEmployeeRequestDTO.salary());
    employee.setRol(updateEmployeeRequestDTO.rol());
    employee.setMultiplex(multiplexProvider.getMultiplexById(updateEmployeeRequestDTO.multiplexId()));

    userRepository.save(user);
    employeeRepository.save(employee);

    return new RegisterResponseDTO(user.getUserType(), user.getUsername(), "Se ha actualizado correctamente el empleado");
}

    public void deleteEmployeeByUniqueCode(Long uniqueCode) {
        EmployeeEntity employee = employeeRepository.findEmployeeEntityByUniqueCode(uniqueCode)
                .orElseThrow(() -> new CinePachoException("No se encontró un empleado con el código único proporcionado"));

        // Valida que solo un MANAGER pueda eliminar EMPLOYEEs de su multiplex.
        accessValidator.validateEmployeeDeletionAccess(employee.getMultiplex().getId());

        if (employee.getUser() != null) {
            UserEntity user = employee.getUser();
            user.setEmployee(null);
            userRepository.save(user);
        }



        employeeRepository.delete(employee);
    }
}
