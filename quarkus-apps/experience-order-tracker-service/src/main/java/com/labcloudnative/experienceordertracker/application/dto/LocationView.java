package com.labcloudnative.experienceordertracker.application.dto;

/**
 * DTO de salida (vista) de una ubicacion, adaptado para el frontend.
 *
 * @param latitude  latitud
 * @param longitude longitud
 * @param address   direccion legible
 */
public record LocationView(
        double latitude,
        double longitude,
        String address
) {
}
