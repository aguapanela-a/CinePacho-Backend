package CinePacho.demo.auth.service;

import CinePacho.demo.auth.dto.response.AuthResponseDTO;
import CinePacho.demo.auth.dto.request.LoginRequestDTO;
import CinePacho.demo.auth.dto.request.RegisterRequestDTO;
import CinePacho.demo.auth.dto.response.RegisterResponseDTO;
import CinePacho.demo.auth.entities.UserEntity;
import CinePacho.demo.auth.repository.UserRepository;
import CinePacho.demo.shared.factory.UserFactoryRegistry;
import CinePacho.demo.shared.user.UserCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserCreationService userCreationService;
    private final UserFactoryRegistry userFactoryRegistry;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    



    public RegisterResponseDTO register(RegisterRequestDTO registerDTO){

        //TODO: lógica de validar correo electrónico
        if (userRepository.existsByEmail(registerDTO.email())) {
            throw new IllegalArgumentException("Email already in use: " + registerDTO.email());
        }
        

        //Crea la entidad Usuario con los datos de registro
        UserEntity user = userCreationService.createUser(
            registerDTO.name(), 
            registerDTO.password(), 
            registerDTO.userType()
        );



        //crea la entidad concreta de manera genérica y la guarda
        userFactoryRegistry.createSpecificEntity(user.getUserType(), user, registerDTO);

        //ya la guarda en la base de datis lo de factory? o se agrega
        // userRepository.save(user);   --> "?"


        //TODO: paquete de seguridad con JwtUtil.java (generar/validar tokens)


        //TODO: lógica de generar el token y devolverlo en el AuthResponseDTO
        


        return new RegisterResponseDTO(
            null, 
            user.getUserType(), 
            user.getUsername()
        );

    }

    public AuthResponseDTO login(LoginRequestDTO loginDTO){

        UserEntity user = userRepository.findByEmail(loginDTO.email())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        //TODO: lógica de validar la contraseña (comparar con la almacenada en la base de datos)
        if (!passwordEncoder.matches(loginDTO.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }


        //TODO: lógica de generar el token y devolverlo en el AuthResponseDTO


        return new AuthResponseDTO(
            null, 
            user.getUserType(), 
            user.getUsername()
        );
    }

}
