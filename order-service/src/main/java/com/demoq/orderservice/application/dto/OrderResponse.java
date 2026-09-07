package com.kcd.orderservice.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO de salida que representa una orden completa en las respuestas de la API.
 *
 * <p>Es la vista publica del agregado {@code Order}; evita exponer
 * directamente el modelo de dominio hacia el exterior.</p>
 *
 * @param id          identificador de la orden
 * @param customerId  identificador del cliente
 * @param status      estado actual de la orden
 * @param totalAmount importe total
 * @param items       lineas de la orden
 * @param createdAt   fecha de creacion
 * @param updatedAt   fecha de ultima modificacion
 */
public record OrderResponse(
        UUID id,
        String customerId,
        String status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}
