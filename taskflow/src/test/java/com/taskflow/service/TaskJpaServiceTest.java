package com.taskflow.service;

import com.taskflow.domain.enums.Category;
import com.taskflow.domain.enums.Priority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.domain.exception.TaskNotFoundException;
import com.taskflow.domain.exception.TaskStateException;
import com.taskflow.dto.request.ActualizarTareaRequest;
import com.taskflow.dto.request.CambiarEstadoRequest;
import com.taskflow.dto.request.CrearTareaRequest;
import com.taskflow.dto.response.TareaResponse;
import com.taskflow.entity.TaskEntity;
import com.taskflow.entity.UserEntity;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests UNITARIOS de TaskJpaService.
 *
 * Concepto @ExtendWith(MockitoExtension.class):
 * Activa Mockito sin levantar Spring. Los @Mock crean objetos
 * simulados y @InjectMocks inyecta esos mocks en el service.
 * Resultado: tests rápidos que prueban solo la lógica del service.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskJpaService — Tests Unitarios")
class TaskJpaServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskJpaService taskJpaService;

    private UserEntity usuarioPrueba;
    private TaskEntity tareaPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new UserEntity(
                "Ana García", "ana@taskflow.com",
                "$2a$10$hash", UserEntity.Rol.ADMIN);

        tareaPrueba = new TaskEntity(
                "Tarea de prueba",
                Priority.ALTA,
                Category.TRABAJO,
                usuarioPrueba);
    }

    // ── crear() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: request válido → retorna TareaResponse con datos correctos")
    void crear_requestValido_retornaTareaResponse() {
        CrearTareaRequest request = new CrearTareaRequest();
        request.setTitulo("Nueva tarea");
        request.setPrioridad(Priority.ALTA);
        request.setCategoria(Category.TRABAJO);
        request.setUsuarioId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(tareaPrueba);

        TareaResponse response = taskJpaService.crear(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitulo()).isEqualTo("Tarea de prueba");
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
    }

    @Test
    @DisplayName("crear: usuarioId inexistente → lanza TaskNotFoundException")
    void crear_usuarioInexistente_lanzaExcepcion() {
        CrearTareaRequest request = new CrearTareaRequest();
        request.setTitulo("Tarea");
        request.setPrioridad(Priority.MEDIA);
        request.setCategoria(Category.PERSONAL);
        request.setUsuarioId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskJpaService.crear(request))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }

    // ── obtenerPorId() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId: ID existente → retorna TareaResponse")
    void obtenerPorId_existente_retornaResponse() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaPrueba));

        TareaResponse response = taskJpaService.obtenerPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getTitulo()).isEqualTo("Tarea de prueba");
    }

    @Test
    @DisplayName("obtenerPorId: ID inexistente → lanza TaskNotFoundException")
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskJpaService.obtenerPorId(99L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    // ── actualizar() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("actualizar: tarea existente → actualiza campos correctamente")
    void actualizar_existente_actualizaCampos() {
        ActualizarTareaRequest request = new ActualizarTareaRequest();
        request.setTitulo("Título actualizado");
        request.setPrioridad(Priority.URGENTE);
        request.setCategoria(Category.ESTUDIO);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaPrueba));

        TareaResponse response = taskJpaService.actualizar(1L, request);

        assertThat(response.getTitulo()).isEqualTo("Título actualizado");
        assertThat(response.getPrioridad()).isEqualTo(Priority.URGENTE);
    }

    @Test
    @DisplayName("actualizar: ID inexistente → lanza TaskNotFoundException")
    void actualizar_inexistente_lanzaExcepcion() {
        ActualizarTareaRequest request = new ActualizarTareaRequest();
        request.setTitulo("Título");
        request.setPrioridad(Priority.BAJA);
        request.setCategoria(Category.OTRO);

        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskJpaService.actualizar(99L, request))
                .isInstanceOf(TaskNotFoundException.class);
    }

    // ── cambiarEstado() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("cambiarEstado: PENDIENTE → EN_PROGRESO es válido")
    void cambiarEstado_pendienteAEnProgreso_esValido() {
        tareaPrueba.setStatus(TaskStatus.PENDIENTE);
        CambiarEstadoRequest request = new CambiarEstadoRequest();
        request.setNuevoEstado(TaskStatus.EN_PROGRESO);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaPrueba));

        TareaResponse response = taskJpaService.cambiarEstado(1L, request);

        assertThat(response.getEstado()).isEqualTo(TaskStatus.EN_PROGRESO);
    }

    @Test
    @DisplayName("cambiarEstado: desde estado COMPLETADA → lanza TaskStateException")
    void cambiarEstado_desdeCompletada_lanzaExcepcion() {
        tareaPrueba.setStatus(TaskStatus.COMPLETADA);
        CambiarEstadoRequest request = new CambiarEstadoRequest();
        request.setNuevoEstado(TaskStatus.EN_PROGRESO);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(tareaPrueba));

        assertThatThrownBy(() -> taskJpaService.cambiarEstado(1L, request))
                .isInstanceOf(TaskStateException.class);
    }

    // ── eliminar() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: ID existente → llama deleteById una vez")
    void eliminar_existente_llamaDeleteById() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskJpaService.eliminar(1L);

        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar: ID inexistente → lanza TaskNotFoundException")
    void eliminar_inexistente_lanzaExcepcion() {
        when(taskRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> taskJpaService.eliminar(99L))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).deleteById(any());
    }
}