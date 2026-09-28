package com.labcloudnative.trackingservice.application.mapper;

import com.labcloudnative.trackingservice.application.dto.AddressResponse;
import com.labcloudnative.trackingservice.application.dto.TrackingEventResponse;
import com.labcloudnative.trackingservice.domain.model.Address;
import com.labcloudnative.trackingservice.domain.model.TrackingEvent;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper de la capa de aplicacion.
 *
 * <p>Traduce el modelo de dominio a los DTOs de salida, aislando la
 * representacion publica de la API del nucleo de negocio.</p>
 */
@ApplicationScoped
public class TrackingDtoMapper {

    /**
     * Convierte un evento de dominio en su DTO de respuesta.
     *
     * @param event evento de dominio
     * @return DTO de salida
     */
    public TrackingEventResponse toResponse(TrackingEvent event) {
        return new TrackingEventResponse(
                event.getId(),
                event.getShipmentId(),
                event.getCoordinates().getLatitude(),
                event.getCoordinates().getLongitude(),
                toResponse(event.getAddress()),
                event.getOccurredAt());
    }

    /**
     * Convierte una direccion de dominio en su DTO de respuesta.
     *
     * @param address direccion de dominio
     * @return DTO de salida de la direccion
     */
    public AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getFormattedAddress(),
                address.getStreet(),
                address.getCity(),
                address.getPostalCode(),
                address.getCountry(),
                address.getPlaceId());
    }
}
