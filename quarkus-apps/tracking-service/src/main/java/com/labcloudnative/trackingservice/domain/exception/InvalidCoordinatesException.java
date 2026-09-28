package com.labcloudnative.trackingservice.domain.exception;

/**
 * Excepcion de dominio que se lanza cuando unas coordenadas no son validas
 * (latitud o longitud fuera de rango).
 *
 * <p>La capa REST la traduce a un codigo HTTP 400 (Bad Request).</p>
 */
public class InvalidCoordinatesException extends RuntimeException {

    public InvalidCoordinatesException(String message) {
        super(message);
    }
}
