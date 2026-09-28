package com.labcloudnative.experienceordertracker.domain.exception;

import java.util.UUID;

/**
 * Excepcion de dominio que se lanza cuando la orden solicitada no existe en
 * order-service.
 *
 * <p>La capa REST la traduce a un codigo HTTP 404 (Not Found).</p>
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID orderId) {
        super("No se encontro la orden con id: " + orderId);
    }
}
