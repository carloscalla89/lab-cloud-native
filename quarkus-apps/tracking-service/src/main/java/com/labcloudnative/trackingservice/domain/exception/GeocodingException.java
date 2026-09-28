package com.labcloudnative.trackingservice.domain.exception;

/**
 * Excepcion de dominio que abstrae cualquier fallo al resolver una direccion
 * mediante el servicio externo de geocodificacion.
 *
 * <p>Es parte fundamental del Anti-Corruption Layer: traduce los errores
 * tecnicos del proveedor externo (timeouts, codigos HTTP, estados como
 * REQUEST_DENIED o ZERO_RESULTS) a un concepto unico y propio del dominio,
 * evitando que esos detalles se propaguen hacia las capas internas.</p>
 *
 * <p>La capa REST la traduce a un codigo HTTP 502 (Bad Gateway).</p>
 */
public class GeocodingException extends RuntimeException {

    public GeocodingException(String message) {
        super(message);
    }

    public GeocodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
