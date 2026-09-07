package com.kcd.trackingservice.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de salida que representa un evento de tracking en las respuestas de la API.
 *
 * @param id         identificador del evento
 * @param shipmentId identificador del envio
 * @param latitude   latitud registrada
 * @param longitude  longitud registrada
 * @param address    direccion resuelta por geocodificacion inversa
 * @param occurredAt instante en que se registro el evento
 */
public record TrackingEventResponse(
        UUID id,
        String shipmentId,
        double latitude,
        double longitude,
        AddressResponse address,
        Instant occurredAt
) {
}
