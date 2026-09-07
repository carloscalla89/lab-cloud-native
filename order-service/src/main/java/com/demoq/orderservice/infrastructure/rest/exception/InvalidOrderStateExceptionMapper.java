package com.kcd.orderservice.infrastructure.rest.exception;

import com.kcd.orderservice.domain.exception.InvalidOrderStateException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduce {@link InvalidOrderStateException} (excepcion de dominio) a una
 * respuesta HTTP 409 (Conflict), indicando que la operacion no es valida
 * para el estado actual de la orden.
 */
@Provider
public class InvalidOrderStateExceptionMapper implements ExceptionMapper<InvalidOrderStateException> {

    @Override
    public Response toResponse(InvalidOrderStateException exception) {
        ErrorResponse body = ErrorResponse.of(
                Response.Status.CONFLICT.getStatusCode(),
                "Conflict",
                exception.getMessage());
        return Response.status(Response.Status.CONFLICT)
                .entity(body)
                .build();
    }
}
