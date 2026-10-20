package com.taskflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger / OpenAPI 3.0.
 *
 * <p><b>Concepto — Swagger / OpenAPI:</b> estándar de documentación
 * automática para APIs REST. Springdoc genera la documentación
 * leyendo las anotaciones de los controllers ({@code @RestController},
 * {@code @Operation}, etc.) y la expone en una UI interactiva.</p>
 *
 * <p>URL de acceso: <a href="http://localhost:8080/swagger-ui.html">
 * http://localhost:8080/swagger-ui.html</a></p>
 *
 * <p>JSON de la spec: <a href="http://localhost:8080/v3/api-docs">
 * http://localhost:8080/v3/api-docs</a></p>
 */
@Configuration
public class SwaggerConfig {

    /**
     * Define los metadatos de la API que aparecen en Swagger UI.
     *
     * @return configuración OpenAPI con título, versión, contacto y servidores
     */
    @Bean
    public OpenAPI taskFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskFlow API")
                        .description("""
                    API REST para gestión de tareas personales y laborales.
                    
                    Permite crear, actualizar, consultar y eliminar tareas
                    organizadas por categorías, prioridades y estados.
                    
                    Proyecto de aprendizaje Java 21 + Spring Boot 4.1.1
                    """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("TaskFlow Team")
                                .email("taskflow@ejemplo.com"))
                        .license(new License()
                                .name("MIT License")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo local")
                ));
    }
}