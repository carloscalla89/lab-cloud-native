package com.kcd.orderservice.domain.exception;

import java.util.UUID;

/**
 * Excepcion de dominio que se lanza cuando una orden solicitada no existe.
 *
 * <p>Pertenece al dominio para que la logica de negocio pueda expresar este
 * error sin acoplarse a la infraestructura. La capa REST la traduce a un
 * codigo HTTP 404.</p>
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID id) {
        super("No se encontro la orden con id: " + id);
    }
}
