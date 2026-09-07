package com.kcd.experienceordertracker.infrastructure.rest.exception;

import com.kcd.experienceordertracker.domain.exception.UpstreamServiceException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduce {@link UpstreamServiceException} a una respuesta HTTP 502 (Bad Gateway),
 * indicando que un microservicio upstream requerido (order-service) fallo.
 */
@Provider
public class UpstreamServiceExceptionMapper implements ExceptionMapper<UpstreamServiceException> {

    @Override
    public Response toResponse(UpstreamServiceException exception) {
        ErrorResponse body = ErrorResponse.of(
                Response.Status.BAD_GATEWAY.getStatusCode(),
                "Bad Gateway",
                exception.getMessage());
        return Response.status(Response.Status.BAD_GATEWAY)
                .entity(body)
                .build();
    }
}
