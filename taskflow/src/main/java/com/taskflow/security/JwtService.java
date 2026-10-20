package com.taskflow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para generar y validar tokens JWT.
 *
 * Concepto JWT: JSON Web Token es un estándar (RFC 7519) para
 * transmitir información de forma segura como un objeto JSON firmado.
 * Estructura: HEADER.PAYLOAD.SIGNATURE
 * - Header: algoritmo de firma (HS256)
 * - Payload: claims (sub, iat, exp, roles)
 * - Signature: HMAC-SHA256(base64(header)+"."+base64(payload), secretKey)
 */
@Service
public class JwtService {

    /** Clave secreta para firmar los tokens — mínimo 256 bits para HS256 */
    @Value("${taskflow.security.jwt-secret}")
    private String secretKey;

    /** Tiempo de expiración del token en milisegundos (24 horas por defecto) */
    @Value("${taskflow.security.jwt-expiration}")
    private long jwtExpiration;

    /**
     * Genera un token JWT para el usuario autenticado.
     * El subject del token es el email del usuario.
     */
    public String generarToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Agregar el rol al payload del token
        claims.put("rol", userDetails.getAuthorities()
                .stream().findFirst()
                .map(Object::toString).orElse("ROLE_USER"));
        return construirToken(claims, userDetails);
    }

    /** Construye el token JWT con todos sus campos */
    private String construirToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())       // email del usuario
                .issuedAt(new Date())                     // fecha de emisión
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())                 // firma con clave secreta
                .compact();
    }

    /** Extrae el email (subject) del token */
    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    /** Verifica que el token sea válido para el usuario dado */
    public boolean esTokenValido(String token, UserDetails userDetails) {
        final String email = extraerEmail(token);
        return email.equals(userDetails.getUsername()) && !estaExpirado(token);
    }

    /** Verifica si el token ha expirado */
    private boolean estaExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    /** Extrae un claim específico del token usando una función */
    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodosLosClaims(token);
        return claimsResolver.apply(claims);
    }

    /** Parsea y verifica la firma del token, devolviendo todos los claims */
    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Convierte la clave secreta en un objeto SecretKey para HMAC-SHA256 */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}