package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.domain.exception.TaskNotFoundException;
import com.taskflow.dto.request.ActualizarTareaRequest;
import com.taskflow.dto.request.CambiarEstadoRequest;
import com.taskflow.dto.request.CrearTareaRequest;
import com.taskflow.dto.response.TareaResponse;
import com.taskflow.security.JwtService;
import com.taskflow.security.UserDetailsServiceImpl;
import com.taskflow.service.TaskJpaService;
import com.taskflow.service.TaskService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test de TaskController con @WebMvcTest.
 *
 * Concepto @WebMvcTest:
 * Levanta SOLO la capa web: controllers, filtros de seguridad y MockMvc.
 * No levanta servicios ni repositorios reales.
 *
 * Concepto @TestConfiguration (sin @EnableWebSecurity):
 * En SB4 con Spring Security 7, usar @EnableWebSecurity en la config de test
 * interfiere con WebMvcAutoConfiguration y el DispatcherServlet no registra
 * los controllers correctamente. La solución es usar @TestConfiguration (sin
 * @EnableWebSecurity): Spring Security auto-config sigue activo y el bean
 * SecurityFilterChain del @TestConfiguration sobreescribe solo la cadena de
 * filtros, sin romper el contexto MVC.
 *
 * Concepto @WithMockUser:
 * Simula un usuario autenticado sin generar un JWT real. Al no haber
 * JwtAuthenticationFilter en la config de test, @WithMockUser funciona
 * directamente con el SecurityContext de Spring Security Test.
 *
 * Concepto @MockitoBean (reemplaza @MockBean de SB3):
 * Spring Framework 7 eliminó @MockBean. Se usa @MockitoBean de
 * org.springframework.test.context.bean.override.mockito
 */
@WebMvcTest(
        value = TaskController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@Import(TaskControllerWebMvcTest.TestSecurityConfig.class)
@ImportAutoConfiguration(ServletWebSecurityAutoConfiguration.class)
@DisplayName("TaskController — Slice Tests @WebMvcTest")
class TaskControllerWebMvcTest {

    /**
     * Configuración de seguridad mínima para el slice test.
     * - @TestConfiguration: se fusiona con el contexto de @WebMvcTest sin
     *   reemplazar WebMvcAutoConfiguration.
     * - Sin @EnableWebSecurity: evita interferencia con el DispatcherServlet.
     * - Sin JwtAuthenticationFilter: @WithMockUser funciona directamente.
     * - authenticationEntryPoint: garantiza 401 (no 403) para requests sin autenticar.
     */
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .exceptionHandling(ex -> ex
                            .authenticationEntryPoint((request, response, authException) ->
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                    )
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.DELETE, "/api/tareas/**").hasRole("ADMIN")
                            .anyRequest().authenticated()
                    );
            return http.build();
        }
    }

    @Autowired
    private WebApplicationContext wac;

    // Se construye en @BeforeEach con springSecurity() para que @WithMockUser funcione
    private MockMvc mockMvc;

    @MockitoBean
    private TaskJpaService taskJpaService;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private TareaResponse tareaRespuesta;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build();

        tareaRespuesta = new TareaResponse();
        tareaRespuesta.setId(1L);
        tareaRespuesta.setTitulo("Tarea de prueba");
        tareaRespuesta.setPrioridad(Priority.ALTA);
        tareaRespuesta.setCategoria(Category.TRABAJO);
        tareaRespuesta.setEstado(TaskStatus.PENDIENTE);
    }

    @Test
    @DisplayName("GET /api/tareas/db sin autenticación → 401")
    void listar_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/tareas/db"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/tareas/db con usuario USER → 200")
    void listar_conUsuarioAutenticado_retorna200() throws Exception {
        Page<TareaResponse> paginaVacia = new PageImpl<>(List.of());
        when(taskJpaService.listar(any(), any(), any(), any(),
                eq(0), eq(10), any(), any()))
                .thenReturn(paginaVacia);

        mockMvc.perform(get("/api/tareas/db"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/tareas/db → 201 con cuerpo correcto")
    void crear_requestValido_retorna201() throws Exception {
        CrearTareaRequest req = new CrearTareaRequest();
        req.setTitulo("Tarea de prueba");
        req.setPrioridad(Priority.ALTA);
        req.setCategoria(Category.TRABAJO);
        req.setUsuarioId(1L);

        when(taskJpaService.crear(any(CrearTareaRequest.class)))
                .thenReturn(tareaRespuesta);

        mockMvc.perform(post("/api/tareas/db")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Tarea de prueba"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        verify(taskJpaService).crear(any(CrearTareaRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/tareas/db con título vacío → 400")
    void crear_tituloVacio_retorna400() throws Exception {
        CrearTareaRequest req = new CrearTareaRequest();
        req.setTitulo("");
        req.setPrioridad(Priority.ALTA);
        req.setCategoria(Category.TRABAJO);
        req.setUsuarioId(1L);

        mockMvc.perform(post("/api/tareas/db")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/tareas/db/1 tarea existente → 200")
    void obtener_existente_retorna200() throws Exception {
        when(taskJpaService.obtenerPorId(1L)).thenReturn(tareaRespuesta);

        mockMvc.perform(get("/api/tareas/db/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Tarea de prueba"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/tareas/db/999 tarea inexistente → 404")
    void obtener_inexistente_retorna404() throws Exception {
        when(taskJpaService.obtenerPorId(999L))
                .thenThrow(new TaskNotFoundException(999L));

        mockMvc.perform(get("/api/tareas/db/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/tareas/db/1 → 200 tarea actualizada")
    void actualizar_existente_retorna200() throws Exception {
        ActualizarTareaRequest req = new ActualizarTareaRequest();
        req.setTitulo("Título actualizado");
        req.setPrioridad(Priority.URGENTE);
        req.setCategoria(Category.ESTUDIO);

        TareaResponse actualizada = new TareaResponse();
        actualizada.setId(1L);
        actualizada.setTitulo("Título actualizado");
        actualizada.setPrioridad(Priority.URGENTE);
        actualizada.setCategoria(Category.ESTUDIO);
        actualizada.setEstado(TaskStatus.PENDIENTE);

        when(taskJpaService.actualizar(eq(1L), any(ActualizarTareaRequest.class)))
                .thenReturn(actualizada);

        mockMvc.perform(put("/api/tareas/db/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Título actualizado"))
                .andExpect(jsonPath("$.prioridad").value("URGENTE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PATCH /api/tareas/db/1/estado PENDIENTE → EN_PROGRESO → 200")
    void cambiarEstado_valido_retorna200() throws Exception {
        CambiarEstadoRequest req = new CambiarEstadoRequest();
        req.setNuevoEstado(TaskStatus.EN_PROGRESO);

        TareaResponse enProgreso = new TareaResponse();
        enProgreso.setId(1L);
        enProgreso.setTitulo("Tarea de prueba");
        enProgreso.setEstado(TaskStatus.EN_PROGRESO);

        when(taskJpaService.cambiarEstado(eq(1L), any(CambiarEstadoRequest.class)))
                .thenReturn(enProgreso);

        mockMvc.perform(patch("/api/tareas/db/1/estado")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROGRESO"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/tareas/db/1 con rol USER → 403 Forbidden")
    void eliminar_conRolUser_retorna403() throws Exception {
        mockMvc.perform(delete("/api/tareas/db/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/tareas/db/1 con rol ADMIN → 204 No Content")
    void eliminar_conRolAdmin_retorna204() throws Exception {
        mockMvc.perform(delete("/api/tareas/db/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(taskJpaService).eliminar(1L);
    }
}