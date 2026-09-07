package com.kcd.experienceordertracker.application.dto;

import java.time.Instant;

/**
 * DTO de salida (vista) de un punto del historial de tracking, para el frontend.
 *
 * @param latitude   latitud
 * @param longitude  longitud
 * @param address    direccion legible
 * @param city       ciudad (puede ser nula)
 * @param country    pais (puede ser nulo)
 * @param occurredAt instante del evento
 */
public record TrackingPointView(
        double latitude,
        double longitude,
        String address,
        String city,
        String country,
        Instant occurredAt
) {
}
