package com.kcd.experienceordertracker.application.dto;

import java.util.List;

/**
 * DTO de salida (vista) del tracking de un envio, adaptado para el frontend.
 *
 * <p>Incluye un indicador {@code available} para que la interfaz sepa si hubo
 * informacion de tracking (degradacion controlada cuando tracking-service no
 * responde) y la {@code currentLocation} ya resuelta para pintar el mapa.</p>
 *
 * @param shipmentId      identificador del envio
 * @param available       indica si hay datos de tracking
 * @param currentLocation ubicacion actual (puede ser nula)
 * @param history         historial de puntos, del mas reciente al mas antiguo
 */
public record TrackingView(
        String shipmentId,
        boolean available,
        LocationView currentLocation,
        List<TrackingPointView> history
) {
}
