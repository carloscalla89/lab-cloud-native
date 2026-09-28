package com.labcloudnative.experienceordertracker.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Modelo de dominio que resume una orden tal como la necesita la experiencia
 * de usuario (frontend), recuperada desde order-service.
 *
 * @param orderId    identificador de la orden
 * @param customerId identificador del cliente
 * @param status     estado de la orden
 * @param totalAmount importe total
 * @param lines      lineas de la orden
 */
public record OrderSummary(
        UUID orderId,
        String customerId,
        String status,
        BigDecimal totalAmount,
        List<OrderLine> lines
) {
    public OrderSummary {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    /**
     * @return cantidad de lineas de la orden (util para vistas de listado)
     */
    public int lineCount() {
        return lines.size();
    }
}
