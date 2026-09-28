package com.labcloudnative.experienceordertracker.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de salida (vista) del caso de uso "crear orden con destino".
 *
 * <p>Combina el resumen de la orden recien creada con el punto de tracking que
 * representa la direccion de entrega, ya geocodificado por tracking-service.</p>
 *
 * <p>Si tracking-service no esta disponible en el momento de la creacion,
 * {@code destinationRegistered} es {@code false} y {@code destination} es
 * {@code null}. La orden existe igualmente en order-service.</p>
 *
 * @param orderId               identificador de la orden creada
 * @param customerId            identificador del cliente
 * @param status                estado inicial de la orden
 * @param totalAmount           importe total
 * @param itemCount             cantidad de lineas
 * @param destinationRegistered indica si el tracking de destino se registro correctamente
 * @param destination           datos de la direccion de entrega (nulo si no disponible)
 */
public record OrderWithDestinationView(
        UUID orderId,
        String customerId,
        String status,
        BigDecimal totalAmount,
        int itemCount,
        boolean destinationRegistered,
        TrackingPointView destination
) {
}
