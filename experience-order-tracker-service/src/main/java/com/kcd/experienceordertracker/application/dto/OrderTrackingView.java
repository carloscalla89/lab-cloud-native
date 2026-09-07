package com.kcd.experienceordertracker.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO de salida (vista) principal del orquestador: combina la orden y su
 * tracking en un unico payload a medida para la pantalla de detalle del
 * frontend.
 *
 * @param orderId     identificador de la orden
 * @param customerId  identificador del cliente
 * @param status      estado de la orden
 * @param totalAmount importe total
 * @param items       lineas de la orden
 * @param tracking    informacion de tracking asociada
 */
public record OrderTrackingView(
        UUID orderId,
        String customerId,
        String status,
        BigDecimal totalAmount,
        List<OrderLineView> items,
        TrackingView tracking
) {
}
