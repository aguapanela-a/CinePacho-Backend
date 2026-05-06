package CinePacho.demo.employeeManageer.service;

import CinePacho.demo.auth.dto.response.RegisterResponseDTO;
import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.employeeManageer.dto.request.RegisterEmployeeRequestDTO;
import CinePacho.demo.shared.user.UserCreationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    private UserCreationService userCreationService;

    @Autowired
    public void setUserCreationService(UserCreationService userCreationService) {
        this.userCreationService = userCreationService;
    }

    public RegisterResponseDTO registerEmployee(RegisterEmployeeRequestDTO registerEmployeeRequestDTO){
        //Creación de UserEntity y entidad concreta de empleado
        UserEntity user = userCreationService.createUser(
                registerEmployeeRequestDTO.name(),
                registerEmployeeRequestDTO.password(),
                registerEmployeeRequestDTO.userType(),
                registerEmployeeRequestDTO.email(),
                registerEmployeeRequestDTO
        );

        user.setEnabled(true);

        return new RegisterResponseDTO(user.getUserType(),user.getUsername(), "Se ha creado correctamente el empleado");
    }
}
