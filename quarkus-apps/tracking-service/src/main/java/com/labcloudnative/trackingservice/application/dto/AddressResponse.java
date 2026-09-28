package com.labcloudnative.trackingservice.application.dto;

/**
 * DTO de salida que representa la direccion resuelta en las respuestas de la API.
 *
 * @param formattedAddress direccion completa formateada
 * @param street           calle (puede ser nula)
 * @param city             ciudad (puede ser nula)
 * @param postalCode       codigo postal (puede ser nulo)
 * @param country          pais (puede ser nulo)
 * @param placeId          identificador del lugar segun el proveedor (puede ser nulo)
 */
public record AddressResponse(
        String formattedAddress,
        String street,
        String city,
        String postalCode,
        String country,
        String placeId
) {
}
