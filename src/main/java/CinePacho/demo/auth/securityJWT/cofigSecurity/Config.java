package CinePacho.demo.auth.securityJWT.cofigSecurity;

import CinePacho.demo.auth.securityJWT.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

    private static final String ADMIN    = "ADMIN";
    private static final String MANAGER  = "MANAGER";
    private static final String BUYER    = "BUYER";
    private static final String EMPLOYEE = "EMPLOYEE";

    // Cajeros y porteros: EMPLOYEE o MANAGER, pero no personal de limpieza/sala.
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
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                        // ── 0. Preflight ─────────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── 1. Auth pública ──────────────────────────────────────────────────────
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/verify"
                        ).permitAll()

                        // ── 2. Snacks ────────────────────────────────────────────────────────────
                        // FIX: Las rutas más específicas PRIMERO; el wildcard al final.
                        // /api/snacks/all  → público (catálogo global)
                        .requestMatchers(HttpMethod.GET, "/api/snacks/all").permitAll()
                        // /api/snacks/{multiplexId} → cajero, comprador y manager necesitan token
                        // SELL_PORTAL_ACCESS cubre BUYER + EMPLOYEE (cajero) + MANAGER
                        .requestMatchers(HttpMethod.GET, "/api/snacks/*")
                                .access(new WebExpressionAuthorizationManager(SELL_PORTAL_ACCESS))
                        // Admin CRUD de snacks
                        .requestMatchers("/api/admin/snacks/**").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/admin/snacks").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers("/api/admin/multiplexes/*/snacks/**").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/admin/multiplexes/*/snacks").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/movie/multiplex/**").permitAll()

                        // ── 3. Películas / cartelera ─────────────────────────────────────────────
                        // FIX: Los selectores (cajero) van ANTES del permitAll de multiplex/**
                        .requestMatchers(HttpMethod.GET, "/api/movie/multiplex/*/selectors")
                                .hasAnyAuthority(BUYER, EMPLOYEE, MANAGER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/movie/multiplex/*/selectors/**")
                                .hasAnyAuthority(BUYER, EMPLOYEE, MANAGER, ADMIN)
                        // Cartelera pública (top8, listado comprador)
                        .requestMatchers(HttpMethod.GET, "/api/movie/trailer/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/topRatedMovies").permitAll()

                        // ── 4. Reviews ───────────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/review/movie/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/*/review").hasAnyAuthority(BUYER, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/*/review/**").hasAuthority(BUYER)

                        // ── 5. Sillas ────────────────────────────────────────────────────────────
                        .requestMatchers("/api/seats/**").hasAnyAuthority(BUYER, EMPLOYEE, MANAGER)

                        // ── 6. Checkout ──────────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/checkout/billings/user/**")
                                .hasAnyAuthority(ADMIN, BUYER, EMPLOYEE, MANAGER)
                        .requestMatchers(HttpMethod.POST, "/api/checkout/stripe")
                                .access(new WebExpressionAuthorizationManager(SELL_PORTAL_ACCESS))
                        .requestMatchers(HttpMethod.POST, "/api/checkout/stripe/success")
                                .access(new WebExpressionAuthorizationManager(SELL_PORTAL_ACCESS))
                        .requestMatchers(HttpMethod.GET, "/api/checkout/stripe/cancel")
                                .access(new WebExpressionAuthorizationManager(SELL_PORTAL_ACCESS))
                        .requestMatchers(HttpMethod.PUT, "/api/checkout/employee/billing/*/scan")
                                .hasAnyAuthority(EMPLOYEE, MANAGER)
                        .requestMatchers("/api/checkout/**")
                                .access(new WebExpressionAuthorizationManager(SELL_PORTAL_ACCESS))

                        // ── 7. Multiplexes ───────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/multiplexes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/multiplexes/**")
                                .hasAnyAuthority(ADMIN, MANAGER, EMPLOYEE, BUYER)
                        .requestMatchers(HttpMethod.POST, "/api/admin/multiplexes").hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/admin/multiplexes/**")
                                .hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/multiplexes/**")
                                .hasAnyAuthority(ADMIN, MANAGER)

                        // ── 8. Salas ─────────────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/admin/rooms/**")
                                .hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.POST, "/api/admin/*/rooms")
                                .hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/rooms/**")
                                .hasAnyAuthority(ADMIN, MANAGER)

                        // ── 9. Empleados ─────────────────────────────────────────────────────────
                        .requestMatchers("/api/admin/register_employee").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/admin/employees/**").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.POST, "/api/admin/update_employee").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/delete_employee/**").hasAnyAuthority(ADMIN, MANAGER)
                        .requestMatchers("/api/admin/movie/**").hasAnyAuthority(ADMIN, MANAGER)

                        // ── 10. Reportes ─────────────────────────────────────────────────────────
                        .requestMatchers("/api/admin/reports/**").hasAnyAuthority(ADMIN, MANAGER)

                        // ── 11. Puntos ───────────────────────────────────────────────────────────
                        .requestMatchers("/api/points/admin/**").hasAuthority(ADMIN)
                        .requestMatchers("/api/points/redeem", "/api/points").hasAuthority(BUYER)
                        .requestMatchers("/api/points/validate").hasAnyAuthority(EMPLOYEE, MANAGER)

                        // ── 12. Admin catch-all ──────────────────────────────────────────────────
                        .requestMatchers("/api/admin/**").hasAuthority(ADMIN)

                        // ── 13. Cualquier ruta no clasificada: denegar ───────────────────────────
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