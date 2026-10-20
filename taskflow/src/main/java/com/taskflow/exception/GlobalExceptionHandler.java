package com.taskflow.exception;

import com.taskflow.domain.exception.ApiException;
import com.taskflow.dto.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para toda la API.
 *
 * <p><b>Concepto Spring — @RestControllerAdvice:</b> intercepta
 * excepciones lanzadas en cualquier controller de la aplicación
 * y las convierte en respuestas HTTP con el formato {@code ApiError}.
 * Sin esto, Spring retornaría un error genérico 500 para todo.</p>
 *
 * <p><b>Concepto Spring — @ExceptionHandler:</b> cada método maneja
 * un tipo específico de excepción y retorna el código HTTP correcto.</p>
 *
 * <p>Flujo: Controller lanza excepción →
 * Spring la intercepta aquí → retorna JSON con ApiError.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación de Bean Validation (@Valid).
     *
     * <p>Se dispara cuando un DTO no pasa las validaciones
     * ({@code @NotBlank}, {@code @Size}, etc.) → HTTP 400.</p>
     *
     * <p><b>Concepto:</b> {@code MethodArgumentNotValidException} contiene
     * todos los errores de campo. Los extraemos con Streams para
     * construir la lista de {@code ErrorDetalle}.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Extraemos todos los errores de campo con Streams
        List<ApiError.ErrorDetalle> detalles = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiError.ErrorDetalle(
                        error.getField(),           // nombre del campo
                        error.getDefaultMessage())) // mensaje de la anotación
                .collect(Collectors.toList());

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validación fallida",
                "VALIDATION_ERROR",
                request.getRequestURI()
        );
        apiError.setErroresDetalle(detalles);

        return ResponseEntity.badRequest().body(apiError);
    }

    /**
     * Maneja todas las excepciones de dominio (ApiException y subclases).
     *
     * <p>Captura {@code TaskNotFoundException} (404),
     * {@code TaskStateException} (409), {@code TaskValidationException} (400).
     * Usamos el {@code httpStatus} que definimos en cada excepción en Fase 1.</p>
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> manejarApiException(
            ApiException ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                ex.getHttpStatus(),
                ex.getMessage(),
                ex.getCodigoError(),
                request.getRequestURI()
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(apiError);
    }

    /**
     * Maneja IllegalArgumentException — errores de argumento inválido.
     * HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> manejarIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                "BAD_REQUEST",
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    /**
     * Manejador genérico — captura cualquier excepción no controlada.
     * HTTP 500 Internal Server Error.
     *
     * <p><b>Buena práctica:</b> nunca exponemos el stack trace al cliente.
     * Retornamos un mensaje genérico y logueamos el detalle internamente.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarGeneral(
            Exception ex,
            HttpServletRequest request) {

        // Log interno (en Fase 6 con tests veremos cómo testear esto)
        System.err.println("Error no controlado: " + ex.getMessage());

        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                "INTERNAL_ERROR",
                request.getRequestURI()
        );

        return ResponseEntity.internalServerError().body(apiError);
    }
}