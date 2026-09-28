package com.labcloudnative.orderservice.domain.exception;

/**
 * Excepcion de dominio que se lanza al intentar una transicion de estado
 * no permitida sobre una orden (por ejemplo, confirmar una orden cancelada).
 *
 * <p>La capa REST la traduce a un codigo HTTP 409 (Conflict).</p>
 */
public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
