package com.labcloudnative.experienceordertracker.domain.model;

/**
 * Composicion de dominio que une una orden con su informacion de tracking.
 *
 * <p>Representa el resultado de orquestar order-service y tracking-service. Es
 * el concepto central del orquestador: combina ambos subdominios en una sola
 * vista coherente, sin contener logica de negocio pesada.</p>
 *
 * @param order    resumen de la orden
 * @param tracking historial de tracking del envio asociado
 */
public record OrderTracking(
        OrderSummary order,
        ShipmentTracking tracking
) {
}
