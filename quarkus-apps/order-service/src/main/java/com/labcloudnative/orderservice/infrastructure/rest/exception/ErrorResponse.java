package com.labcloudnative.orderservice.infrastructure.rest.exception;

import java.time.Instant;

/**
 * Cuerpo estandar de error devuelto por la API.
 *
 * @param status    codigo HTTP
 * @param error     descripcion corta del tipo de error
 * @param message   mensaje detallado
 * @param timestamp momento en que se genero el error
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp
) {
    /**
     * Crea un ErrorResponse con la marca de tiempo actual.
     *
     * @param status  codigo HTTP
     * @param error   descripcion corta
     * @param message mensaje detallado
     * @return el error listo para serializar
     */
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, Instant.now());
    }
}
