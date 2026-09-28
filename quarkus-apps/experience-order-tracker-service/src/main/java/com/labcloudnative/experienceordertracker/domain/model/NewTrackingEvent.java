package com.labcloudnative.experienceordertracker.domain.model;

/**
 * Modelo de dominio (comando) para registrar un evento de tracking a traves
 * de tracking-service.
 *
 * @param shipmentId identificador del envio
 * @param latitude   latitud del evento
 * @param longitude  longitud del evento
 */
public record NewTrackingEvent(
        String shipmentId,
        double latitude,
        double longitude
) {
}
