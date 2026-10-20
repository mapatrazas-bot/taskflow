package com.taskflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de TaskFlow.
 * Los datos de prueba ahora los carga Flyway (V2__datos_iniciales.sql)
 * al arrancar — no necesitamos el CommandLineRunner.
 */
@SpringBootApplication
public class TaskflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskflowApplication.class, args);
		System.out.println("""
            
            ═══════════════════════════════════════════════
              TaskFlow API — Fase 4: Spring Data JPA
              Servidor:   http://localhost:8080
              Swagger:    http://localhost:8080/swagger-ui.html
              H2 Console: http://localhost:8080/h2-console
              Actuator:   http://localhost:8080/actuator/health
              API BD:     http://localhost:8080/api/tareas/db
            ═══════════════════════════════════════════════
            """);
	}
}