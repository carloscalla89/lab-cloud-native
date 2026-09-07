package com.kcd.experienceordertracker.infrastructure.rest.exception;

import com.kcd.experienceordertracker.domain.exception.OrderNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduce {@link OrderNotFoundException} a una respuesta HTTP 404 (Not Found).
 */
@Provider
public class OrderNotFoundExceptionMapper implements ExceptionMapper<OrderNotFoundException> {

    @Override
    public Response toResponse(OrderNotFoundException exception) {
        ErrorResponse body = ErrorResponse.of(
                Response.Status.NOT_FOUND.getStatusCode(),
                "Not Found",
                exception.getMessage());
        return Response.status(Response.Status.NOT_FOUND)
                .entity(body)
                .build();
    }
}
