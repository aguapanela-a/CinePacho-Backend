package CinePacho.demo.cofigSecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class Config {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())  //Desabilidar el CSRF porque se usará JWT
                .authorizeHttpRequests(auth -> auth //abrir config de autorización
                        .requestMatchers("/api/auth/**").permitAll() //Permitir cualquier request al endpoint de Auth
                        .anyRequest().authenticated() //El resto de request requieren de token
                )
//                .sessionManagement(session -> session //Decide como se recordarán los usuarios autenticados
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //No crear sesiones de usuario jsessionid para que sea el JWT el unico que se valide
//                )
                //Filtro para validar JWT
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
