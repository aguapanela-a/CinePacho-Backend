package CinePacho.demo.auth.securityJWT.cofigSecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.http.HttpMethod;

import CinePacho.demo.auth.securityJWT.JwtAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
public class Config {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailService;

    public Config(JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailsService userDetailService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailService = userDetailService;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Obtiene el valor de la variable de entorno 'FRONTEND_URL' definida en el servidor (ej. Railway)
        String frontendUrl = System.getenv("FRONTEND_URL");

        // Si la variable existe, la usa como origen permitido; de lo contrario, usa localhost por defecto para desarrollo local
        config.setAllowedOrigins(List.of(frontendUrl != null ? frontendUrl : "http://localhost:5173"));

        System.out.println("CORS Origin configurado como: " + (frontendUrl != null ? frontendUrl : "http://localhost:5173"));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())  //Desabilidar el CSRF porque se usará JWT
                .authorizeHttpRequests(auth -> auth //abrir config de autorización
                        .requestMatchers(
                            "/api/auth/register",
                            "/api/auth/login",
                            "/api/auth/verify"
                        ).permitAll()//Permitir cualquier request al endpoint de Auth
                        // Endpoints compartidos entre BUYER y EMPLOYEE
                        .requestMatchers("/api/seats/**").hasAnyAuthority("BUYER", "EMPLOYEE", "MANAGER", "ADMIN") //permitir acceso a empleados para pruebas, pero validación por alcance en servicio
                        // Snacks visibles para compradores autenticados
                        .requestMatchers(HttpMethod.GET, "/api/snacks/**").hasAnyAuthority("BUYER", "EMPLOYEE", "MANAGER", "ADMIN") //permitir acceso a empleados para pruebas, pero validación por alcance en servicio
                        // Checkout de compra para compradores
                        .requestMatchers("/api/checkout/**").hasAnyAuthority("BUYER", "EMPLOYEE", "MANAGER", "ADMIN") //permitir acceso a checkout a empleados para pruebas, pero validación por alcance en servicio
                        // Multiplex: crear y eliminar sólo admin
                        .requestMatchers(HttpMethod.POST, "/api/admin/multiplexes").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/multiplexes/**").hasAuthority("ADMIN")
                        // Multiplex: consulta/actualización permitida a gerente y admin (validación por alcance en servicio)
                        .requestMatchers("/api/admin/multiplexes").hasAnyAuthority("ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/multiplexes/**").hasAnyAuthority("ADMIN", "MANAGER")
                        // Salas y personal: gerente y admin (validación por alcance en servicio)
                        .requestMatchers("/api/admin/*/rooms").hasAnyAuthority("ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/rooms/**").hasAnyAuthority("ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/register_employee").hasAnyAuthority("ADMIN", "MANAGER")
                        // Películas: gerente y admin (validación por alcance en servicio)
                        .requestMatchers("/api/admin/movie/**").hasAnyAuthority("ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/**").hasAuthority ("ADMIN") //a ese endpoint solamente puede entrar el admin
                        .anyRequest().authenticated() //El resto de request requieren de token
                )
                .sessionManagement(session -> session //Decide como se recordarán los usuarios autenticados
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //No crear sesiones de usuario jsessionid para que sea el JWT el unico que se valide
                )
                .userDetailsService(userDetailService)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) //Agregar el filtro de JWT antes del filtro de autenticación por defecto
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
