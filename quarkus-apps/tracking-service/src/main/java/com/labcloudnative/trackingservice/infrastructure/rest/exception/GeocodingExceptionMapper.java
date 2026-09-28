package com.labcloudnative.trackingservice.infrastructure.rest.exception;

import com.labcloudnative.trackingservice.domain.exception.GeocodingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduce {@link GeocodingException} a una respuesta HTTP 502 (Bad Gateway),
 * indicando que la dependencia externa (Google Maps) fallo o no devolvio datos.
 */
@Provider
public class GeocodingExceptionMapper implements ExceptionMapper<GeocodingException> {

    @Override
    public Response toResponse(GeocodingException exception) {
        ErrorResponse body = ErrorResponse.of(
                Response.Status.BAD_GATEWAY.getStatusCode(),
                "Bad Gateway",
                exception.getMessage());
        return Response.status(Response.Status.BAD_GATEWAY)
                .entity(body)
                .build();
    }
}
