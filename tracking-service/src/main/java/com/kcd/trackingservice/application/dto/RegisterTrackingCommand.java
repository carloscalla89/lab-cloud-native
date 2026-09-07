package com.kcd.trackingservice.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Comando de entrada para el caso de uso "registrar evento de tracking".
 *
 * <p>Recibe el envio y las coordenadas. La validacion declarativa garantiza
 * que los rangos geograficos sean correctos antes de llegar al dominio.</p>
 *
 * @param shipmentId identificador del envio
 * @param latitude   latitud en grados [-90, 90]
 * @param longitude  longitud en grados [-180, 180]
 */
public record RegisterTrackingCommand(

        @NotBlank(message = "El shipmentId es obligatorio")
        String shipmentId,

        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90.0", message = "La latitud minima es -90")
        @DecimalMax(value = "90.0", message = "La latitud maxima es 90")
        Double latitude,

        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180.0", message = "La longitud minima es -180")
        @DecimalMax(value = "180.0", message = "La longitud maxima es 180")
        Double longitude
) {
}
