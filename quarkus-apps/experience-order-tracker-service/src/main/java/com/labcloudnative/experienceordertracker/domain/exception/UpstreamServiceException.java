package com.labcloudnative.experienceordertracker.domain.exception;

/**
 * Excepcion de dominio que abstrae cualquier fallo al invocar un microservicio
 * upstream (order-service o tracking-service).
 *
 * <p>Aisla los detalles tecnicos del transporte (timeouts, codigos HTTP,
 * errores de red) en un unico concepto propio del orquestador. La capa REST la
 * traduce a un codigo HTTP 502 (Bad Gateway).</p>
 */
public class UpstreamServiceException extends RuntimeException {

    public UpstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
