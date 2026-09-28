package com.labcloudnative.experienceordertracker.domain.model;

import java.time.Instant;

/**
 * Modelo de dominio de un punto de tracking, recuperado desde tracking-service.
 *
 * <p>Aplana la informacion relevante de un evento de tracking (coordenadas y
 * los datos de direccion que interesan al frontend) en la representacion
 * propia del orquestador.</p>
 *
 * @param latitude         latitud del evento
 * @param longitude        longitud del evento
 * @param formattedAddress direccion completa resuelta
 * @param city             ciudad (puede ser nula)
 * @param country          pais (puede ser nulo)
 * @param occurredAt       instante en que ocurrio el evento
 */
public record TrackingPoint(
        double latitude,
        double longitude,
        String formattedAddress,
        String city,
        String country,
        Instant occurredAt
) {
}
