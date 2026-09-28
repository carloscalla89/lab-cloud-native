package com.labcloudnative.experienceordertracker.domain.model;

import java.util.List;
import java.util.Optional;

/**
 * Modelo de dominio que agrupa el historial de tracking de un envio.
 *
 * <p>Los eventos se asumen ordenados del mas reciente al mas antiguo (tal como
 * los entrega tracking-service), por lo que el primero representa la ubicacion
 * actual.</p>
 *
 * @param shipmentId identificador del envio
 * @param events     puntos de tracking, del mas reciente al mas antiguo
 */
public record ShipmentTracking(
        String shipmentId,
        List<TrackingPoint> events
) {
    public ShipmentTracking {
        events = events == null ? List.of() : List.copyOf(events);
    }

    /**
     * @return {@code true} si hay al menos un punto de tracking
     */
    public boolean isAvailable() {
        return !events.isEmpty();
    }

    /**
     * @return la ubicacion actual (evento mas reciente), si existe
     */
    public Optional<TrackingPoint> currentLocation() {
        return events.isEmpty() ? Optional.empty() : Optional.of(events.get(0));
    }

    /**
     * Crea un tracking vacio para un envio (sin eventos). Se usa como
     * degradacion controlada cuando tracking-service no responde.
     *
     * @param shipmentId identificador del envio
     * @return un tracking sin eventos
     */
    public static ShipmentTracking empty(String shipmentId) {
        return new ShipmentTracking(shipmentId, List.of());
    }
}
