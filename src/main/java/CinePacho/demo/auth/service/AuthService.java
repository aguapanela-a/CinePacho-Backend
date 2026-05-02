package CinePacho.demo.auth.service;

import CinePacho.demo.auth.dto.AuthResponseDTO;
import CinePacho.demo.auth.dto.RegisterDTO;
import CinePacho.demo.auth.entities.UserEntity;
import CinePacho.demo.shared.factory.UserFactoryRegistry;
import CinePacho.demo.shared.user.UserCreationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserCreationService userCreationService;
    private final UserFactoryRegistry userFactoryRegistry;

    @Autowired
    public AuthService(UserCreationService userCreationService, UserFactoryRegistry userFactoryRegistry) {
        this.userCreationService = userCreationService;
        this.userFactoryRegistry = userFactoryRegistry;
    }

    @Transactional
    public AuthResponseDTO register(RegisterDTO registerDTO){


        UserEntity user = userCreationService.createUser(registerDTO.name(), registerDTO.password(), registerDTO.userType());

        //TODO: lógica de validar correo electrónico: busca que no exista y envía correo de verificación

        //crea la entidad concreta de manera genérica y la guarda
        userFactoryRegistry.createSpecificEntity(user.getUserType(), user, registerDTO);

        //TODO: el registro CREA el usuario y llama a login para que genere el token de acceso



        return new AuthResponseDTO(null, user.getUserType(), user.getUsername());
    }


    //TODO: crear paquete de seguridad con JwtUtil.java (generar/validar tokens) - y AuthenticationManager
    public AuthResponseDTO login(String username, String password){
        //TODO: usar authenticationManager.authenticate para verificar credenciales
        //TODO: traer al User
        //TODO: generar el token con la info del user ( uuid del usuario, el rol y el nombre) y devolverlo en el AuthResponseDTO

        return new AuthResponseDTO(null, null, null);
    }
}
