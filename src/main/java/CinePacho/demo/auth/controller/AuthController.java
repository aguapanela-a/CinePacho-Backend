package CinePacho.demo.auth.controller;

import CinePacho.demo.auth.dto.response.RegisterResponseDTO;
import CinePacho.demo.shared.registerData.RegisterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import CinePacho.demo.auth.dto.request.LoginRequestDTO;
import CinePacho.demo.auth.dto.response.AuthResponseDTO;
import CinePacho.demo.auth.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
//
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(
            @Valid @RequestBody RegisterData dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    // El usuario llega aquí desde el link del correo
    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }
}

//eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcmNoaXZvc3VuaXZlcnNpZGFkMTBAZ21haWwuY29tIiwibmFtZSI6ImFyY2hpdm9zdW5pdmVyc2lkYWQxMEBnbWFpbC5jb20iLCJ1c2VyVHlwZSI6IkJVWUVSIiwiaWF0IjoxNzc5NzE1NDI2LCJleHAiOjE3Nzk3MTkwMjZ9.aSBvuuueuyWZ76w2a0GRd5J0_jhnwhuP1Db3mdZu--E