package com.kcd.experienceordertracker.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada (comando) para registrar un evento de tracking a traves del BFF.
 *
 * @param shipmentId identificador del envio
 * @param latitude   latitud del evento (-90 a 90)
 * @param longitude  longitud del evento (-180 a 180)
 */
public record RegisterTrackingViewCommand(
        @NotBlank String shipmentId,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude
) {
}
