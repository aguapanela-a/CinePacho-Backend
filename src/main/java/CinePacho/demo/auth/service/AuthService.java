package CinePacho.demo.auth.service;

import CinePacho.demo.auth.dto.response.AuthResponseDTO;
import CinePacho.demo.auth.dto.request.LoginRequestDTO;
import CinePacho.demo.auth.dto.request.RegisterRequestDTO;
import CinePacho.demo.auth.dto.response.RegisterResponseDTO;
import CinePacho.demo.auth.entities.UserEntity;
import CinePacho.demo.auth.entities.VerificationToken;
import CinePacho.demo.auth.repository.UserRepository;
import CinePacho.demo.auth.repository.VerificationTokenRepository;
import CinePacho.demo.shared.factory.UserFactoryRegistry;
import CinePacho.demo.shared.user.UserCreationService;
import io.jsonwebtoken.Jwt;
import jakarta.transaction.Transactional;
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
    private final EmailService emailService;
    private final VerificationTokenRepository tokenRepository;
    // private final JwtService jwtService;
    // private final JwtUtil jwtUtil;


    @Transactional
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


        // Crear token de verificación
        VerificationToken verificationToken = new VerificationToken(user);
        tokenRepository.save(verificationToken);


        
        //enviar correo de verificación
        emailService.sendVerificationEmail(user, verificationToken.getToken());

        return new RegisterResponseDTO(
            verificationToken.getToken(), 
            user.getUserType(), 
            user.getUsername(),
            "User registered successfully. Please check your email to verify your account."
        );
    }

    @Transactional
    public String verifyEmail(String token) {
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (vToken.isUsed()) {
            throw new RuntimeException("El token ya fue usado");
        }

        if (vToken.isExpired()) {
            throw new RuntimeException("El token ha expirado");
        }

        // Activar usuario
        UserEntity user = vToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);


        // Marcar token como usado
        vToken.setUsed(true);
        tokenRepository.save(vToken);

        return "Cuenta confirmada exitosamente. Ya puedes iniciar sesión.";
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
