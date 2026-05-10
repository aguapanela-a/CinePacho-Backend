package CinePacho.demo.auth.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import CinePacho.demo.auth.entities.user.UserEntity;
import lombok.RequiredArgsConstructor;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    public void sendVerificationEmail(UserEntity user, String token) {
        String link = baseUrl + "/api/auth/verify?token=" + token;

        String body = """
        {
            "sender": {"name": "CinePacho", "email": "ericksbp23@gmail.com"},
            "to": [{"email": "%s"}],
            "subject": "Confirma tu cuenta",
            "textContent": "Hola %s,\\n\\nHaz clic en el siguiente enlace para confirmar tu cuenta:\\n%s\\n\\nEl enlace expira en 24 horas.\\n\\nSi no creaste esta cuenta, ignora este mensaje."
        }
        """.formatted(user.getEmail(), user.getUsername(), link);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("api-key", brevoApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 201) {
                throw new RuntimeException("Error Brevo: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo: " + e.getMessage());
        }
    }


}