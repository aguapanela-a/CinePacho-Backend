package CinePacho.demo.auth.securityJWT;

import CinePacho.demo.shared.serviceSecurity.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component

public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si no hay header o no empieza con "Bearer ", dejamos pasar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                System.out.println("Usuario autenticado: " + SecurityContextHolder.getContext().getAuthentication().getName());
                System.out.println("Autoridades encontradas: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            } else {
                System.out.println("OJO: Nadie se autenticó en el filtro.");
            }
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7); // quita "Bearer "
        final String email = jwtService.extractEmail(token);

        System.out.println("Token: " + token);
        System.out.println("Email: " + email);

        // Si tiene email y no hay autenticación previa en el contexto
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            System.out.println("2. Usuario encontrado en BD: " + userDetails.getUsername());
            System.out.println("3. Autoridades en BD: " + userDetails.getAuthorities());

            if (jwtService.isTokenValid(token, userDetails)) {

                System.out.println("4. ¡El token es válido matemáticamente!");

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // Registra la autenticación en el contexto de Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }else{
                System.out.println("4. ERROR: isTokenValid devolvió FALSE.");
            }
        }


        // Justo antes de que el filtro termine su trabajo
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            System.out.println("FINAL: Autorizado en Spring como: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        } else {
            System.out.println("FINAL: El contexto llegó vacío al final del filtro.");
        }

        filterChain.doFilter(request, response);
    }
}
