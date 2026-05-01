package CinePacho.demo.auth.service;

import CinePacho.demo.auth.dto.AuthResponseDTO;
import CinePacho.demo.auth.dto.RegisterDTO;
import CinePacho.demo.auth.entities.UserEntity;
import CinePacho.demo.shared.factory.UserFactoryRegistry;
import CinePacho.demo.shared.user.UserCreationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserCreationService userCreationService;
    private final UserFactoryRegistry userFactoryRegistry;

    @Autowired
    public AuthService(UserCreationService userCreationService, UserFactoryRegistry userFactoryRegistry) {
        this.userCreationService = userCreationService;
        this.userFactoryRegistry = userFactoryRegistry;
    }

    public AuthResponseDTO register(RegisterDTO registerDTO){

        //TODO: lógica de validar correo electrónico

        //Crea la entidad Usuario con los datos de registro
        UserEntity user = userCreationService.createUser(registerDTO.name(), registerDTO.password(), registerDTO.userType());

        //crea la entidad concreta de manera genérica y la guarda
        userFactoryRegistry.createEspecificEntity(user.getUserType(), user, registerDTO);

        //TODO: paquete de seguridad con JwtUtil.java (generar/validar tokens)
        //TODO: lógica de generar el token y devolverlo en el AuthResponseDTO

        return new AuthResponseDTO(null, user.getUserType(), user.getUsername());

    }
}
