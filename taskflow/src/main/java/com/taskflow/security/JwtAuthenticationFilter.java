package com.taskflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT que intercepta CADA request HTTP.
 *
 * Concepto OncePerRequestFilter: garantiza que el filtro se ejecute
 * exactamente una vez por request, sin importar el número de dispatches.
 *
 * Flujo:
 * 1. Leer el header "Authorization: Bearer <token>"
 * 2. Extraer y validar el token JWT
 * 3. Cargar el usuario desde la BD
 * 4. Si es válido, registrar la autenticación en el SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Obtener el header Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Si no tiene "Bearer ", pasar al siguiente filtro sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token (quitar "Bearer ")
        final String jwt = authHeader.substring(7);
        final String email = jwtService.extraerEmail(jwt);

        // 4. Si tiene email y no está autenticado aún en este request
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 5. Validar el token
            if (jwtService.esTokenValido(jwt, userDetails)) {

                // 6. Crear el token de autenticación de Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // credentials (no necesarias post-auth)
                                userDetails.getAuthorities()   // roles del usuario
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. Registrar en el SecurityContext — el usuario está autenticado
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 8. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}