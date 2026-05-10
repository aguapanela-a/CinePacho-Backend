package CinePacho.demo.auth.service;

import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import com.resend.Resend;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import CinePacho.demo.auth.entities.user.UserEntity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.resend-API-key}")
    private String resendKey;

    public void sendVerificationEmail(UserEntity user, String token) throws ResendException {
        String link = baseUrl + "/api/auth/verify?token=" + token;

        String message = "Hola " + user.getUsername() + ",\n\n" +
                "Haz clic en el siguiente enlace para confirmar tu cuenta:\n" +
                link + "\n\n" +
                "El enlace expira en 24 horas.\n\n" +
                "Si no creaste esta cuenta, ignora este mensaje.";

        Resend resend = new Resend(resendKey);

        CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                        .from("\"CinePacho <onboarding@resend.dev>\"")
                                .to(user.getEmail())
                                        .subject("Confirma tu cuenta")
                                                .text(message)
                                                        .build();

        resend.emails().send(emailOptions);
    }


}