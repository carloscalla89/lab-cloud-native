package com.kcd.trackingservice.infrastructure.rest.exception;

import com.kcd.trackingservice.domain.exception.TrackingEventNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduce {@link TrackingEventNotFoundException} a una respuesta HTTP 404 (Not Found).
 */
@Provider
public class TrackingEventNotFoundExceptionMapper implements ExceptionMapper<TrackingEventNotFoundException> {

    @Override
    public Response toResponse(TrackingEventNotFoundException exception) {
        ErrorResponse body = ErrorResponse.of(
                Response.Status.NOT_FOUND.getStatusCode(),
                "Not Found",
                exception.getMessage());
        return Response.status(Response.Status.NOT_FOUND)
                .entity(body)
                .build();
    }
}
