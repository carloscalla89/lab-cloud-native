package com.labcloudnative.experienceordertracker.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de salida (vista) ligero de una orden, para la pantalla de listado del
 * frontend. No incluye tracking ni el detalle de lineas para mantener la
 * respuesta liviana.
 *
 * @param orderId     identificador de la orden
 * @param customerId  identificador del cliente
 * @param status      estado de la orden
 * @param totalAmount importe total
 * @param itemCount   cantidad de lineas
 */
public record OrderSummaryView(
        UUID orderId,
        String customerId,
        String status,
        BigDecimal totalAmount,
        int itemCount
) {
}
