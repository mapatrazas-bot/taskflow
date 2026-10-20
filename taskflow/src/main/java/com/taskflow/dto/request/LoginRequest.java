package com.taskflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** DTO para el inicio de sesión */
public record LoginRequest(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}