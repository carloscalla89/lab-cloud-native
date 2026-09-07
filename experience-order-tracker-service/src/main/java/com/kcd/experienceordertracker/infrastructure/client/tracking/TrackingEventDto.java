package com.kcd.experienceordertracker.infrastructure.client.tracking;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO externo: representa un evento de tracking devuelto por tracking-service.
 *
 * <p>Modela el contrato del upstream y vive exclusivamente en infraestructura.
 * El adaptador lo traduce al modelo de dominio {@code TrackingPoint}.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrackingEventDto(
        UUID id,
        String shipmentId,
        double latitude,
        double longitude,
        AddressDto address,
        Instant occurredAt
) {

    /** DTO externo de la direccion resuelta segun tracking-service. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressDto(
            String formattedAddress,
            String street,
            String city,
            String postalCode,
            String country,
            String placeId
    ) {
    }
}
