package com.taskflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing).
 *
 * <p><b>Concepto REST — CORS:</b> los navegadores bloquean por seguridad
 * las peticiones HTTP desde un dominio diferente al del servidor.
 * CORS define qué orígenes, métodos y headers están permitidos.
 * Sin esta configuración, un frontend en localhost:3000 no podría
 * llamar a nuestra API en localhost:8080.</p>
 *
 * <p><b>Concepto Spring — WebMvcConfigurer:</b> interfaz que permite
 * personalizar la configuración de Spring MVC sin reemplazarla.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configura las reglas CORS para todos los endpoints de la API.
     *
     * <p>En desarrollo permitimos cualquier origen. En producción
     * (Fase 8 - AWS) lo restringiremos al dominio real.</p>
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")           // aplica a todos los endpoints /api/
                .allowedOrigins("*")                 // cualquier origen (dev)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")                 // cualquier header
                .exposedHeaders("X-Total-Count")     // header personalizado expuesto
                .maxAge(3600);                       // cache del preflight 1 hora
    }
}