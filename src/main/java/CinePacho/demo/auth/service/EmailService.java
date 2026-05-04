package CinePacho.demo.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import CinePacho.demo.auth.entities.UserEntity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(UserEntity user, String token) {
        String link = baseUrl + "/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Confirma tu cuenta");
        message.setText(
            "Hola " + user.getUsername() + ",\n\n" +
            "Haz clic en el siguiente enlace para confirmar tu cuenta:\n" +
            link + "\n\n" +
            "El enlace expira en 24 horas.\n\n" +
            "Si no creaste esta cuenta, ignora este mensaje."
        );

        mailSender.send(message);
    }
}