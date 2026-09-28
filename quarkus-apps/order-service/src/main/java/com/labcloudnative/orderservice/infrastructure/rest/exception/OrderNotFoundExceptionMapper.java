package com.labcloudnative.orderservice.infrastructure.rest.exception;

import com.labcloudnative.orderservice.domain.exception.OrderNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduce {@link OrderNotFoundException} (excepcion de dominio) a una
 * respuesta HTTP 404 con un cuerpo de error estandar.
 *
 * <p>Mantiene la traduccion de errores en la capa de infraestructura, sin
 * filtrar detalles de dominio hacia el protocolo HTTP de forma manual.</p>
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
