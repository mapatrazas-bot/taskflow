package com.taskflow.controller;

import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.entity.UserEntity;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación.
 * Endpoints públicos — no requieren token JWT.
 *
 * POST /auth/register → crea nuevo usuario, devuelve JWT
 * POST /auth/login    → autentica usuario, devuelve JWT
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Registro e inicio de sesión")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registro de nuevo usuario.
     * Hashea la contraseña con BCrypt antes de guardarla.
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        // Verificar que el email no esté en uso
        if (userRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Crear usuario con contraseña hasheada
        UserEntity usuario = new UserEntity(
                request.nombre(),
                request.email(),
                passwordEncoder.encode(request.password()),  // NUNCA texto plano
                UserEntity.Rol.USER
        );
        userRepository.save(usuario);

        // Generar y devolver el JWT
        String token = jwtService.generarToken(usuario);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.of(token, usuario.getEmail(),
                        usuario.getRol().name(), 86400000L));
    }

    /**
     * Inicio de sesión.
     * AuthenticationManager verifica email + contraseña contra la BD.
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener JWT")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        // Autenticar — lanza excepción si las credenciales son incorrectas
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(), request.password()));

        UserEntity usuario = (UserEntity) auth.getPrincipal();
        String token = jwtService.generarToken(usuario);

        return ResponseEntity.ok(
                AuthResponse.of(token, usuario.getEmail(),
                        usuario.getRol().name(), 86400000L));
    }
}