package com.kcd.experienceordertracker.domain.model;

import java.util.List;

/**
 * Modelo de dominio (comando) para crear una orden a traves de order-service.
 *
 * @param customerId identificador del cliente
 * @param items      lineas de la orden a crear
 */
public record NewOrderRequest(
        String customerId,
        List<NewOrderItem> items
) {
    public NewOrderRequest {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
