package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.request.CrearTareaRequest;
import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración E2E completo.
 *
 * Concepto @SpringBootTest + @AutoConfigureMockMvc:
 * Levanta el contexto completo de Spring (BD H2, Flyway, Security).
 * MockMvc simula peticiones HTTP reales sin levantar un servidor.
 *
 * Flujo E2E: register → login → crear → listar → actualizar → eliminar
 *
 * Nota Spring Boot 4.x: ObjectMapper ya no se registra automáticamente
 * como bean en tests con @SpringBootTest + webmvc. Se instancia directamente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TaskController — Test E2E Integración")
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // En Spring Boot 4.x el ObjectMapper no se registra como bean en tests —
    // se instancia directamente para evitar UnsatisfiedDependencyException
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Token y ID compartidos entre tests del flujo E2E
    private static String tokenUser;
    private static String tokenAdmin;
    private static Long tareaCreada;

    // ── Paso 1: Seguridad ─────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("E2E Paso 1a: GET /api/tareas/db sin token → 401")
    void sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/tareas/db"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @Order(2)
    @DisplayName("E2E Paso 1b: register usuario USER → 201 con token")
    void register_retorna201ConToken() throws Exception {
        String email = "e2e_user@taskflow.com";
        RegisterRequest req = new RegisterRequest("E2E User", email, "password123");

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        tokenUser = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("token").asText();
        assertThat(tokenUser).isNotBlank();
    }

    @Test
    @Order(3)
    @DisplayName("E2E Paso 1c: login ADMIN (Ana García) → 200 con rol ADMIN")
    void loginAdmin_retorna200() throws Exception {
        LoginRequest req = new LoginRequest("ana@taskflow.com", "admin123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andReturn();

        tokenAdmin = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("token").asText();
        assertThat(tokenAdmin).isNotBlank();
    }

    // ── Paso 2: Crear tarea ───────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("E2E Paso 2: POST /api/tareas/db con token → 201 tarea creada")
    void crear_conToken_retorna201() throws Exception {
        // Necesita token del paso anterior
        if (tokenUser == null) register_retorna201ConToken();

        CrearTareaRequest req = new CrearTareaRequest();
        req.setTitulo("Tarea E2E de prueba");
        req.setPrioridad(Priority.ALTA);
        req.setCategoria(Category.TRABAJO);
        req.setUsuarioId(1L);

        MvcResult result = mockMvc.perform(post("/api/tareas/db")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenUser)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Tarea E2E de prueba"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn();

        tareaCreada = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();
        assertThat(tareaCreada).isPositive();
    }

    // ── Paso 3: Listar tareas ─────────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("E2E Paso 3: GET /api/tareas/db con token → 200 lista tareas")
    void listar_conToken_retorna200() throws Exception {
        if (tokenUser == null) register_retorna201ConToken();

        mockMvc.perform(get("/api/tareas/db")
                        .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    // ── Paso 4: Filtros y paginacion ──────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("E2E Paso 4: GET con filtro categoria=TRABAJO → solo tareas TRABAJO")
    void listar_conFiltroCategoria_retornaFiltrado() throws Exception {
        if (tokenUser == null) register_retorna201ConToken();

        mockMvc.perform(get("/api/tareas/db")
                        .param("categoria", "TRABAJO")
                        .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ── Paso 5: Cambiar estado ────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("E2E Paso 5: PATCH estado PENDIENTE → EN_PROGRESO")
    void cambiarEstado_pendienteAEnProgreso() throws Exception {
        if (tokenUser == null) register_retorna201ConToken();
        if (tareaCreada == null) crear_conToken_retorna201();

        String body = "{\"nuevoEstado\":\"EN_PROGRESO\"}";

        mockMvc.perform(patch("/api/tareas/db/" + tareaCreada + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenUser)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROGRESO"));
    }

    // ── Paso 6: Control de roles ──────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("E2E Paso 6a: DELETE con rol USER → 403 Forbidden")
    void delete_conRolUser_retorna403() throws Exception {
        if (tokenUser == null) register_retorna201ConToken();
        if (tareaCreada == null) crear_conToken_retorna201();

        mockMvc.perform(delete("/api/tareas/db/" + tareaCreada)
                        .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(9)
    @DisplayName("E2E Paso 6b: DELETE con rol ADMIN → 204 No Content")
    void delete_conRolAdmin_retorna204() throws Exception {
        if (tokenAdmin == null) loginAdmin_retorna200();
        if (tareaCreada == null) crear_conToken_retorna201();

        mockMvc.perform(delete("/api/tareas/db/" + tareaCreada)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }
}