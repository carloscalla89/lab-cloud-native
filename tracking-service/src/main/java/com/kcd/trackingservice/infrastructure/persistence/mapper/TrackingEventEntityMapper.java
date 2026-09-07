package com.kcd.trackingservice.infrastructure.persistence.mapper;

import com.kcd.trackingservice.domain.model.Address;
import com.kcd.trackingservice.domain.model.Coordinates;
import com.kcd.trackingservice.domain.model.TrackingEvent;
import com.kcd.trackingservice.infrastructure.persistence.entity.TrackingEventEntity;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper de persistencia.
 *
 * <p>Traduce entre el modelo de dominio ({@link TrackingEvent}) y la entidad
 * JPA ({@link TrackingEventEntity}), aplanando/reconstruyendo los objetos de
 * valor {@link Coordinates} y {@link Address}.</p>
 */
@ApplicationScoped
public class TrackingEventEntityMapper {

    /**
     * Construye una entidad JPA a partir de un evento de dominio.
     *
     * @param event evento de dominio
     * @return entidad lista para persistir
     */
    public TrackingEventEntity toEntity(TrackingEvent event) {
        TrackingEventEntity entity = new TrackingEventEntity();
        entity.id = event.getId();
        entity.shipmentId = event.getShipmentId();
        entity.latitude = event.getCoordinates().getLatitude();
        entity.longitude = event.getCoordinates().getLongitude();

        Address address = event.getAddress();
        entity.formattedAddress = address.getFormattedAddress();
        entity.street = address.getStreet();
        entity.city = address.getCity();
        entity.postalCode = address.getPostalCode();
        entity.country = address.getCountry();
        entity.placeId = address.getPlaceId();

        entity.occurredAt = event.getOccurredAt();
        return entity;
    }

    /**
     * Reconstruye un evento de dominio a partir de su entidad JPA.
     *
     * @param entity entidad recuperada de la base de datos
     * @return el evento de dominio rehidratado
     */
    public TrackingEvent toDomain(TrackingEventEntity entity) {
        Coordinates coordinates = new Coordinates(entity.latitude, entity.longitude);
        Address address = new Address(
                entity.formattedAddress,
                entity.street,
                entity.city,
                entity.postalCode,
                entity.country,
                entity.placeId);
        return new TrackingEvent(entity.id, entity.shipmentId, coordinates, address, entity.occurredAt);
    }
}
