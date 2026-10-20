package com.taskflow.domain.record;

import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.domain.model.Task;

import java.util.List;

/**
 * Estadísticas calculadas de una lista de tareas.
 *
 * <p><b>Concepto Java — Record como resultado de cálculo:</b>
 * {@code TaskStats} es un snapshot inmutable de estadísticas.
 * Se crea una sola vez vía el factory method {@code calcular()}.</p>
 *
 * <p><b>Concepto Java — Streams para agregación:</b> el método
 * {@code calcular()} demuestra múltiples operaciones de Stream:
 * {@code filter}, {@code count}, {@code anyMatch}.</p>
 */
public record TaskStats(
        long total,
        long pendientes,
        long enProgreso,
        long completadas,
        long canceladas,
        long vencidas
) {

    /**
     * Calcula el porcentaje de tareas completadas.
     *
     * <p>Maneja el caso de división por cero cuando no hay tareas.</p>
     *
     * @return porcentaje de 0.0 a 100.0
     */
    public double porcentajeCompletado() {
        if (total == 0) return 0.0;
        return (completadas * 100.0) / total;
    }

    /**
     * Calcula todas las estadísticas de una lista de tareas usando Streams.
     *
     * <p><b>Concepto Java — Stream.filter().count():</b> para contar elementos
     * que cumplen una condición sin crear colecciones intermedias.</p>
     *
     * @param tareas lista de tareas sobre las que calcular estadísticas
     * @return record con todas las estadísticas calculadas
     */
    public static TaskStats calcular(List<Task> tareas) {
        long total       = tareas.size();
        long pendientes  = tareas.stream().filter(t -> t.getStatus() == TaskStatus.PENDIENTE).count();
        long enProgreso  = tareas.stream().filter(t -> t.getStatus() == TaskStatus.EN_PROGRESO).count();
        long completadas = tareas.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETADA).count();
        long canceladas  = tareas.stream().filter(t -> t.getStatus() == TaskStatus.CANCELADA).count();
        long vencidas    = tareas.stream().filter(Task::estaVencida).count();

        return new TaskStats(total, pendientes, enProgreso, completadas, canceladas, vencidas);
    }
}