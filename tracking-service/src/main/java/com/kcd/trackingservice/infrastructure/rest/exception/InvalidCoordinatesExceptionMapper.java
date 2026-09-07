package com.kcd.trackingservice.infrastructure.rest.exception;

import com.kcd.trackingservice.domain.exception.InvalidCoordinatesException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduce {@link InvalidCoordinatesException} a una respuesta HTTP 400 (Bad Request).
 */
@Provider
public class InvalidCoordinatesExceptionMapper implements ExceptionMapper<InvalidCoordinatesException> {

    @Override
    public Response toResponse(InvalidCoordinatesException exception) {
        ErrorResponse body = ErrorResponse.of(
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Bad Request",
                exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(body)
                .build();
    }
}
