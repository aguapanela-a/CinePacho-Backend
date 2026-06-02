package CinePacho.demo.auth.securityJWT.cofigSecurity;

import CinePacho.demo.auth.securityJWT.JwtAuthenticationFilter;
import com.beust.ah.A;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class Config {

    private static final String ADMIN = "ADMIN";
    private static final String MANAGER = "MANAGER";
    private static final String BUYER = "BUYER";
    private static final String EMPLOYEE = "EMPLOYEE";
    // Cambio: excluir CLEANER y ROOM_ATTENDANT de ventas aunque tengan autoridad EMPLOYEE.
    private static final String SELL_PORTAL_ACCESS =
            "hasAnyAuthority('BUYER','EMPLOYEE','MANAGER') and !hasAnyAuthority('CLEANER','ROOM_ATTENDANT')";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailService;

    public Config(JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailsService userDetailService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailService = userDetailService;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        String frontendUrl = System.getenv("FRONTEND_URL");

        config.setAllowedOrigins(List.of(frontendUrl != null ? frontendUrl : "http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Autenticacion publica.
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/verify"
                        ).permitAll()
                        // Top 10 peliculas (home) publico
                        .requestMatchers(HttpMethod.GET, "/api/topRatedMovies").permitAll()

                        // Visualizacion publica de reviews por pelicula.
                        .requestMatchers(HttpMethod.GET, "/api/review/movie/**").permitAll()

                        // Portal buyer y portal empleado: cartelera, sillas, snacks y checkout.
                        // Todos los employees (EMPLOYEE y MANAGER) pueden acceder, EXCEPTO CLEANER y ROOM_ATTENDANT.
                        .requestMatchers(HttpMethod.GET, "/api/movie/multiplex/**")
                        .access(new WebExpressionAuthorizationManager(SELL_PORTAL_ACCESS))
                        .requestMatchers("/api/movie/multiplex/*/selectors").hasAnyAuthority(BUYER, EMPLOYEE, MANAGER)
                        .requestMatchers("/api/movie/multiplex/*/selectors/**").hasAnyAuthority(BUYER, EMPLOYEE, MANAGER)
                        .requestMatchers("/api/movie/trailer/**").hasAnyAuthority(BUYER, EMPLOYEE, MANAGER)
                        .requestMatchers("/api/topRatedMovies").permitAll()
                        .requestMatchers("/api/seats/**").hasAnyAuthority(BUYER, EMPLOYEE, MANAGER)
                        .requestMatchers("/api/snacks")
                        .access(new WebExpressionAuthorizationManager(SELL_PORTAL_ACCESS))
                        .requestMatchers("/api/checkout/**")
                        .access(new WebExpressionAuthorizationManager(SELL_PORTAL_ACCESS))

                        // Reviews propias de compradores. El servicio valida identidad del BUYER.
                        .requestMatchers(HttpMethod.GET, "/api/*/review").hasAnyAuthority(BUYER, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/*/review/**").hasAuthority(BUYER)

                        // Administracion de multiplex: ADMIN global, MANAGER limitado por AccessValidator.
                        .requestMatchers(HttpMethod.POST, "/api/multiplexes").hasAnyAuthority(BUYER, ADMIN, EMPLOYEE, MANAGER)
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/multiplexes/**").hasAnyAuthority(ADMIN, EMPLOYEE, MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/admin/multiplexes", "/api/admin/multiplexes/**")
                        .hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.PUT, "/api/admin/multiplexes/**").hasAnyAuthority(ADMIN, MANAGER)


                        // Administracion por multiplex asignado.
                        .requestMatchers(HttpMethod.POST, "/api/admin/*/rooms").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/rooms/**").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers("/api/admin/register_employee").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/admin/employees/**").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.POST, "/api/admin/update_employee").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers("/api/admin/movie/**").hasAnyAuthority(ADMIN, MANAGER)

                        // Snacks aun no estan modelados por multiplex, por eso el CRUD queda global para ADMIN y MANAGER local.
                        .requestMatchers("/api/admin/snacks/**").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/admin/snacks").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.POST, "/api/checkout/stripe").access(new WebExpressionAuthorizationManager(SELL_PORTAL_ACCESS))
                        .requestMatchers(HttpMethod.POST, "/api/checkout/stripe/success").access(new WebExpressionAuthorizationManager(SELL_PORTAL_ACCESS))
                        .requestMatchers(HttpMethod.GET, "/api/checkout/stripie/cancel").access(new WebExpressionAuthorizationManager(SELL_PORTAL_ACCESS))
                        .requestMatchers(HttpMethod.PUT, "/api/checkout/employee/billing/*/scan").hasAnyAuthority(EMPLOYEE, MANAGER)

                        // Endpoints de points: admin y buyer
                        .requestMatchers("/api/points/admin/**").hasAuthority(ADMIN)
                        .requestMatchers("/api/points/redeem", "/api/points").hasAuthority(BUYER)
                        .requestMatchers("/api/points/validate").hasAnyAuthority(EMPLOYEE, MANAGER)

                        .requestMatchers("/api/snacks/**").hasAnyAuthority(BUYER, EMPLOYEE, MANAGER)

                        // Cualquier endpoint administrativo no clasificado queda reservado para ADMIN.
                        .requestMatchers("/api/admin/**").hasAuthority(ADMIN)

                        // Si aparece una ruta nueva, debe clasificarse antes de quedar accesible.
                        .anyRequest().denyAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .userDetailsService(userDetailService)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
