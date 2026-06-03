package CinePacho.demo.auth.service;

import CinePacho.demo.auth.dto.response.AuthResponseDTO;
import CinePacho.demo.auth.dto.request.LoginRequestDTO;
import CinePacho.demo.auth.dto.response.RegisterResponseDTO;
import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.auth.entities.token.VerificationToken;
import CinePacho.demo.auth.entities.customers.repository.BuyerRepository;
import CinePacho.demo.shared.auxiliaryClass.EmailService;
import CinePacho.demo.shared.user.UserRepository;
import CinePacho.demo.auth.repository.VerificationTokenRepository;
import CinePacho.demo.shared.serviceSecurity.JwtService;
import CinePacho.demo.shared.enumeration.UserType;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.shared.registerData.RegisterData;
import CinePacho.demo.shared.user.UserCreationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserCreationService userCreationService;
    private final UserRepository userRepository;
    private final BuyerRepository buyerRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenRepository tokenRepository;
    private final JwtService jwtService;
    // private final JwtUtil jwtUtil;


    @Transactional
    public RegisterResponseDTO register(RegisterData registerDTO){

        // Eliminar espacios en blanco y convertir el email a minúsculas para evitar problemas de formato
        registerDTO.email().toLowerCase().trim();
        registerDTO.name().trim();
        registerDTO.password().trim();

        if (userRepository.existsByEmail(registerDTO.email())) {
            throw new CinePachoException("Email already in use: " + registerDTO.email());
        }

        if (registerDTO.userType() != UserType.BUYER) {
            // El registro público sólo permite compradores
            throw new CinePachoException("Solo se permite registrar compradores desde este endpoint");
        }

        //Crea la entidad Usuario con los datos de registro y la guarda
        //y crea la entidad concreta de manera genérica
        UserEntity user = userCreationService.createUser(
            registerDTO.name(), 
            registerDTO.password(), 
            registerDTO.userType(),
            registerDTO.email(),
            registerDTO
        );


        // Crear token temporal de verificación
        VerificationToken verificationToken = new VerificationToken(user);
        tokenRepository.save(verificationToken);

        
        //enviar correo de verificación
        try{
            emailService.sendVerificationEmail(user, verificationToken.getToken());

        } catch (Exception e) {
            throw new CinePachoException("Error al enviar el correo de verificación a " + user.getEmail() + ": " + e.getMessage());
        }


        return new RegisterResponseDTO(
            user.getUserType(),
            user.getUsername(),
            "User registered successfully. Please check your email to verify your account."
        );
    }

    @Transactional
    public String verifyEmail(String token) {
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new CinePachoException("Token inválido"));

        if (vToken.isUsed()) {
            throw new CinePachoException("El token ya fue usado");
        }

        if (vToken.isExpired()) {
            throw new CinePachoException("El token ha expirado");
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


    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginDTO){

        UserEntity user = userRepository.findByEmail(loginDTO.email())
            .orElseThrow(() -> new CinePachoException("Usuario no encontrado"));

        if(!user.isEnabled()) {
            throw new CinePachoException("Debes verificar tu correo antes de iniciar sesión");
        }
    
        if (!passwordEncoder.matches(loginDTO.password(), user.getPassword())) {
            throw new CinePachoException("Credenciales incorrectas");
        }

        java.util.UUID responseId = user.getUserId();
        if (user.getUserType() == UserType.BUYER) {
            responseId = buyerRepository.getBuyerByEmail(user.getEmail())
                .orElseThrow(() -> new CinePachoException("Buyer not found for email: " + user.getEmail()))
                .getBuyerId();
        }


        return new AuthResponseDTO(
            jwtService.generateToken(user),
                user.getUserType(),
                user.getUsername(),
                user.getEmployee() != null ? user.getEmployee().getMultiplex().getId() : null,
                responseId
        );
    }

}
