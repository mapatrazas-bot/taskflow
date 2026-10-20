package com.taskflow.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de Spring Security.
 *
 * Concepto SecurityFilterChain (Spring Boot 3+/4):
 * Reemplaza el antiguo WebSecurityConfigurerAdapter.
 * Define qué URLs son públicas, cuáles requieren autenticación
 * y qué filtros se aplican.
 *
 * Concepto STATELESS: JWT es sin estado — no usamos sesiones HTTP.
 * Cada request lleva su propio token, el servidor no guarda estado.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Define las reglas de seguridad HTTP.
     * Incluye manejo de errores 401 (sin token) y 403 (sin permisos).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF — no necesario con JWT (stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // 401 cuando no hay token / 403 cuando no tiene permisos
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Sin token o token inválido → 401 Unauthorized
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"status\":401," +
                                            "\"error\":\"No autenticado\"," +
                                            "\"mensaje\":\"Se requiere token JWT. Usa Authorization: Bearer <token>\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // Token válido pero sin el rol necesario → 403 Forbidden
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"status\":403," +
                                            "\"error\":\"Acceso denegado\"," +
                                            "\"mensaje\":\"No tienes permisos para esta operación\"}");
                        })
                )

                // Reglas de autorización por URL
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos — no requieren token
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        // Solo ADMIN puede eliminar tareas
                        .requestMatchers(HttpMethod.DELETE, "/api/tareas/**").hasRole("ADMIN")
                        // Todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated()
                )

                // Permitir frames para H2 Console
                .headers(headers -> headers.frameOptions(fo -> fo.disable()))

                // Sin sesiones HTTP — JWT es stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Usar nuestro UserDetailsService + BCrypt
                .authenticationProvider(authenticationProvider())

                // Agregar filtro JWT antes del filtro de usuario/contraseña estándar
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCryptPasswordEncoder — hash seguro para contraseñas.
     * Factor de costo 10 = ~100ms por hash (resistente a fuerza bruta).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Proveedor de autenticación: BD + BCrypt.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager — usado en AuthController para el login.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}