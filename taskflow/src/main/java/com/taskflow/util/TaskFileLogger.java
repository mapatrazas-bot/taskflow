package com.taskflow.util;

import com.taskflow.domain.model.Task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Logger de operaciones de tareas sobre archivo de texto.
 *
 * <p><b>Concepto Java — Manejo de archivos con NIO2:</b> usamos la API
 * moderna {@code java.nio.file} ({@code Path}, {@code Paths}, {@code Files})
 * en lugar de la API legacy {@code java.io.File}. NIO2 es más robusta,
 * multiplataforma y más expresiva.</p>
 *
 * <p><b>Concepto Java — try-with-resources:</b> el bloque
 * {@code try (BufferedWriter w = ...)} garantiza que el writer se cierra
 * automáticamente aunque ocurra una excepción, evitando fugas de recursos.</p>
 *
 * <p><b>Concepto Java — LocalDateTime + DateTimeFormatter:</b>
 * cada entrada del log incluye un timestamp formateado.</p>
 */
public class TaskFileLogger {

    private static final DateTimeFormatter FORMATO_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Ruta del archivo de log relativa al directorio de trabajo. */
    private final Path archivoLog;

    /**
     * Crea el logger, asegurando que el directorio y el archivo existan.
     *
     * <p><b>Concepto Java — Files.createDirectories():</b> crea la
     * carpeta "logs/" y todas las carpetas padre necesarias si no existen.
     * Es idempotente: no falla si ya existen.</p>
     *
     * @throws RuntimeException si no se puede crear el directorio o archivo
     */
    public TaskFileLogger() {
        this.archivoLog = Paths.get("logs", "taskflow.log");
        try {
            // Crea el directorio logs/ si no existe (NIO2)
            Files.createDirectories(archivoLog.getParent());
            // Crea el archivo si no existe, sin sobrescribirlo si ya existe
            if (!Files.exists(archivoLog)) {
                Files.createFile(archivoLog);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el logger: " + e.getMessage(), e);
        }
    }

    /**
     * Escribe una línea en el log con nivel e información.
     *
     * <p><b>Concepto Java — try-with-resources:</b> {@code BufferedWriter}
     * implementa {@code AutoCloseable}, por lo que se cierra automáticamente
     * al salir del bloque {@code try}, incluso si hay una excepción.</p>
     *
     * <p>{@code FileWriter(ruta, true)} abre en modo append:
     * no sobreescribe el contenido existente.</p>
     *
     * @param nivel   nivel de log (INFO, WARN, ERROR)
     * @param mensaje contenido de la entrada
     */
    public void log(String nivel, String mensaje) {
        String entrada = String.format("[%s] [%s] %s%n",
                LocalDateTime.now().format(FORMATO_TIMESTAMP),
                nivel,
                mensaje);

        // try-with-resources → cierre automático garantizado
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(archivoLog.toFile(), true))) {
            writer.write(entrada);
        } catch (IOException e) {
            // Si falla el log, no interrumpimos la operación principal
            System.err.println("⚠ No se pudo escribir en el log: " + e.getMessage());
        }
    }

    /**
     * Registra una operación sobre una tarea en el log.
     *
     * @param accion descripción de la acción (ej: "CREADA", "ACTUALIZADA")
     * @param tarea  tarea sobre la que se realizó la acción
     */
    public void logTarea(String accion, Task tarea) {
        log("INFO", String.format("TAREA_%s → %s", accion, tarea.getDescripcionCorta()));
    }

    /**
     * Lee las últimas {@code n} líneas del archivo de log.
     *
     * <p><b>Concepto Java — Files.readAllLines():</b> lee todas las líneas
     * del archivo como {@code List<String>}. Especificamos {@code UTF-8}
     * para compatibilidad multiplataforma.</p>
     *
     * @param n número de líneas a retornar desde el final
     * @return lista con las últimas {@code n} líneas
     */
    public List<String> leerUltimasLineas(int n) {
        try {
            List<String> todasLasLineas = Files.readAllLines(archivoLog, StandardCharsets.UTF_8);
            if (todasLasLineas.isEmpty()) {
                return Collections.emptyList();
            }
            int inicio = Math.max(0, todasLasLineas.size() - n);
            return new ArrayList<>(todasLasLineas.subList(inicio, todasLasLineas.size()));
        } catch (IOException e) {
            System.err.println("⚠ No se pudo leer el log: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Vacía el contenido del archivo de log.
     *
     * <p><b>Concepto Java — Files.writeString():</b> sobreescribe el
     * archivo con contenido vacío (Java 11+).</p>
     */
    public void limpiarLog() {
        try {
            Files.writeString(archivoLog, "");
            log("INFO", "Log limpiado.");
        } catch (IOException e) {
            System.err.println("⚠ No se pudo limpiar el log: " + e.getMessage());
        }
    }

    /** @return la ruta absoluta del archivo de log */
    public String getRutaLog() {
        return archivoLog.toAbsolutePath().toString();
    }
}