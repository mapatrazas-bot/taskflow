package com.taskflow.domain.model;

import com.taskflow.domain.enums.TaskStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Representa un usuario propietario de tareas en el sistema.
 *
 * <p><b>Concepto Java — Herencia:</b> también extiende {@code BaseEntity},
 * demostrando que la misma clase base sirve para entidades distintas.</p>
 *
 * <p><b>Concepto Java — Polimorfismo:</b> {@code toString()} y
 * {@code getDescripcionCorta()} tienen implementación propia,
 * diferente a la de {@code Task}, aunque compartan la misma firma.</p>
 *
 * <p><b>Concepto Java — Streams:</b> los métodos de consulta de tareas
 * del usuario usan Streams para filtrar, contar y transformar datos.</p>
 */
public class User extends BaseEntity {

    private String nombre;
    private String email;

    /**
     * Lista de tareas del usuario.
     * Inicializada vacía para evitar NullPointerException.
     * Concepto: Collections — List<Task> tipada con Generics.
     */
    private final List<Task> tareas = new ArrayList<>();

    /**
     * @param nombre nombre completo del usuario
     * @param email  dirección de correo electrónico
     */
    public User(String nombre, String email) {
        super();
        this.nombre = nombre;
        this.email = email;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getNombre() { return nombre; }
    public String getEmail() { return email; }

    /**
     * Retorna una copia defensiva de la lista de tareas.
     * Nadie puede modificar la lista interna desde afuera.
     *
     * <p><b>Concepto Java — Encapsulamiento:</b> retornamos una vista
     * no modificable para proteger el estado interno del usuario.</p>
     *
     * @return lista inmutable de tareas del usuario
     */
    public List<Task> getTareas() {
        return Collections.unmodifiableList(tareas);
    }

    // ── Mutadores ────────────────────────────────────────────────────────────

    /** Actualiza el nombre del usuario. */
    public void setNombre(String nombre) {
        this.nombre = nombre;
        marcarActualizado();
    }

    /**
     * Agrega una tarea a la lista del usuario.
     *
     * @param tarea tarea a asociar con este usuario
     */
    public void agregarTarea(Task tarea) {
        if (tarea != null) {
            tareas.add(tarea);
        }
    }

    // ── Consultas con Streams ─────────────────────────────────────────────────

    /**
     * Retorna las tareas que aún no están en un estado final.
     *
     * <p><b>Concepto Java — Streams + Lambda:</b> {@code stream().filter()}
     * con lambda que evalúa el estado de cada tarea.</p>
     *
     * @return lista de tareas en estado PENDIENTE o EN_PROGRESO
     */
    public List<Task> tareasActivas() {
        return tareas.stream()
                .filter(tarea -> tarea.getStatus().isActivo())
                .collect(Collectors.toList());
    }

    /**
     * Cuenta las tareas que tienen un estado específico.
     *
     * <p><b>Concepto Java — Stream.count():</b> operación terminal
     * que retorna el número de elementos que pasaron el filtro.</p>
     *
     * @param status estado por el que filtrar
     * @return número de tareas con ese estado
     */
    public long contarPorStatus(TaskStatus status) {
        return tareas.stream()
                .filter(tarea -> tarea.getStatus() == status)
                .count();
    }

    /**
     * Indica si el usuario tiene tareas vencidas sin atender.
     *
     * <p><b>Concepto Java — Stream.anyMatch():</b> shortcircuit — para
     * en el primer elemento que cumpla la condición.</p>
     *
     * @return {@code true} si hay al menos una tarea vencida
     */
    public boolean tieneVencidas() {
        return tareas.stream().anyMatch(Task::estaVencida);
    }

    // ── BaseEntity abstracto ─────────────────────────────────────────────────

    /**
     * <b>Concepto Java — Polimorfismo:</b> misma firma que en Task,
     * distinto comportamiento según la clase concreta.
     */
    @Override
    public String getDescripcionCorta() {
        return String.format("User[%d] '%s' (%s) | tareas: %d",
                getId(), nombre, email, tareas.size());
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, nombre='%s', email='%s', tareas=%d}",
                getId(), nombre, email, tareas.size());
    }
}