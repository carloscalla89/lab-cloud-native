package com.kcd.trackingservice.domain.exception;

import java.util.UUID;

/**
 * Excepcion de dominio que se lanza cuando un evento de tracking no existe.
 *
 * <p>La capa REST la traduce a un codigo HTTP 404 (Not Found).</p>
 */
public class TrackingEventNotFoundException extends RuntimeException {

    public TrackingEventNotFoundException(UUID id) {
        super("No se encontro el evento de tracking con id: " + id);
    }
}
