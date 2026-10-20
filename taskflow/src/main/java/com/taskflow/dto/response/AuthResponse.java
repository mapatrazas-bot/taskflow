package com.taskflow.dto.response;

/** DTO de respuesta que devuelve el token JWT tras login/registro */
public record AuthResponse(
        String token,
        String tipo,
        String email,
        String rol,
        long expiraEn
) {
    /** Factory method para crear la respuesta estándar */
    public static AuthResponse of(String token, String email, String rol, long expiraEn) {
        return new AuthResponse(token, "Bearer", email, rol, expiraEn);
    }
}