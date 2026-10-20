package com.taskflow.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utilidades de fecha y hora para el sistema TaskFlow.
 *
 * <p><b>Concepto Java — Clase utilitaria:</b> constructor privado para
 * evitar instanciación. Todos los métodos son estáticos.</p>
 *
 * <p><b>Concepto Java — java.time API:</b> usamos la API moderna de
 * fechas (Java 8+): {@code LocalDate}, {@code LocalDateTime},
 * {@code DateTimeFormatter}, {@code ChronoUnit}. Nunca usamos
 * las clases antiguas {@code Date} o {@code Calendar}.</p>
 */
public final class DateTimeUtils {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /** Constructor privado — esta clase no debe ser instanciada. */
    private DateTimeUtils() {
        throw new UnsupportedOperationException("Clase utilitaria, no instanciar.");
    }

    /**
     * Formatea una fecha en formato legible "dd/MM/yyyy".
     *
     * @param fecha fecha a formatear
     * @return cadena formateada, o "Sin fecha" si es {@code null}
     */
    public static String formatearFecha(LocalDate fecha) {
        if (fecha == null) return "Sin fecha";
        return fecha.format(FORMATO_FECHA);
    }

    /**
     * Formatea una fecha y hora en formato "dd/MM/yyyy HH:mm:ss".
     *
     * @param fechaHora fecha y hora a formatear
     * @return cadena formateada, o "Sin fecha" si es {@code null}
     */
    public static String formatearFechaHora(LocalDateTime fechaHora) {
        if (fechaHora == null) return "Sin fecha";
        return fechaHora.format(FORMATO_FECHA_HORA);
    }

    /**
     * Calcula los días entre dos fechas.
     *
     * <p><b>Concepto Java — ChronoUnit.DAYS.between():</b> forma
     * idiomática de calcular diferencias de tiempo.</p>
     *
     * @param inicio fecha de inicio
     * @param fin    fecha de fin
     * @return número de días (negativo si {@code fin} es anterior a {@code inicio})
     */
    public static long diasEntre(LocalDate inicio, LocalDate fin) {
        return ChronoUnit.DAYS.between(inicio, fin);
    }

    /**
     * Verifica si una fecha ya pasó (es anterior a hoy).
     *
     * @param fecha fecha a evaluar
     * @return {@code true} si la fecha es antes de hoy
     */
    public static boolean esFechaPasada(LocalDate fecha) {
        return fecha != null && fecha.isBefore(LocalDate.now());
    }

    /**
     * Genera una descripción legible del tiempo transcurrido desde una fecha/hora.
     * Ejemplos: "hace 5 minutos", "hace 2 horas", "hace 3 días".
     *
     * <p><b>Concepto Java — ChronoUnit:</b> usamos múltiples unidades
     * de tiempo para dar la descripción más precisa.</p>
     *
     * @param desde fecha/hora de inicio
     * @return descripción legible del tiempo transcurrido
     */
    public static String tiempoTranscurrido(LocalDateTime desde) {
        LocalDateTime ahora = LocalDateTime.now();
        long minutos = ChronoUnit.MINUTES.between(desde, ahora);
        long horas   = ChronoUnit.HOURS.between(desde, ahora);
        long dias    = ChronoUnit.DAYS.between(desde, ahora);

        if (minutos < 1)   return "hace un momento";
        if (minutos < 60)  return "hace " + minutos + " minuto(s)";
        if (horas < 24)    return "hace " + horas + " hora(s)";
        if (dias < 30)     return "hace " + dias + " día(s)";

        long meses = ChronoUnit.MONTHS.between(desde, ahora);
        return "hace " + meses + " mes(es)";
    }
}