package com.taskflow.config;

import com.taskflow.service.TaskService;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.sql.DataSource;

/**
 * Configuracion principal de la aplicacion TaskFlow.
 * @EnableJpaAuditing activa auditoria automatica de JPA.
 */
@EnableJpaAuditing
@Configuration
@EnableConfigurationProperties(AppConfig.TaskflowProperties.class)
public class AppConfig {

    @Value("${taskflow.app.mensaje-bienvenida}")
    private String mensajeBienvenida;

    @Value("${taskflow.app.version}")
    private String version;

    /**
     * Bean del TaskService en memoria (Fases 1-3).
     */
    @Bean
    public TaskService taskService() {
        return new TaskService();
    }

    /**
     * Flyway forzado por codigo para garantizar migraciones en Spring Boot 4.x.
     * Concepto: FlywayMigrationInitializer ejecuta los scripts SQL versionados
     * (V1__, V2__...) al arrancar la aplicacion, antes de que Hibernate
     * inicialice el schema.
     */
    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
    }

    public String getMensajeBienvenida() { return mensajeBienvenida; }
    public String getVersion() { return version; }

    @ConfigurationProperties(prefix = "taskflow.app")
    public static class TaskflowProperties {
        private int maxTareasPorUsuario;
        private String version;
        private String mensajeBienvenida;

        public int getMaxTareasPorUsuario() { return maxTareasPorUsuario; }
        public void setMaxTareasPorUsuario(int v) { this.maxTareasPorUsuario = v; }
        public String getVersion() { return version; }
        public void setVersion(String v) { this.version = v; }
        public String getMensajeBienvenida() { return mensajeBienvenida; }
        public void setMensajeBienvenida(String v) { this.mensajeBienvenida = v; }

        @Override
        public String toString() {
            return String.format(
                    "TaskflowProperties{maxTareas=%d, version='%s', bienvenida='%s'}",
                    maxTareasPorUsuario, version, mensajeBienvenida);
        }
    }
}