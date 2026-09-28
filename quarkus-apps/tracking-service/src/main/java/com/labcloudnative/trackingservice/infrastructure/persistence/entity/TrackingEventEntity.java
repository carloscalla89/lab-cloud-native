package com.labcloudnative.trackingservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de persistencia (JPA) para un evento de tracking.
 *
 * <p>Mapea la tabla {@code tracking_events}. Es un detalle de infraestructura,
 * separado de la entidad de dominio {@code TrackingEvent} para no acoplar el
 * nucleo de negocio a JPA. Las coordenadas y la direccion se aplanan en
 * columnas.</p>
 */
@Entity
@Table(name = "tracking_events")
public class TrackingEventEntity {

    /** Identificador asignado por el dominio (UUID), sin generacion automatica. */
    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "shipment_id", nullable = false)
    public String shipmentId;

    @Column(name = "latitude", nullable = false)
    public double latitude;

    @Column(name = "longitude", nullable = false)
    public double longitude;

    @Column(name = "formatted_address", nullable = false, length = 512)
    public String formattedAddress;

    @Column(name = "street")
    public String street;

    @Column(name = "city")
    public String city;

    @Column(name = "postal_code", length = 64)
    public String postalCode;

    @Column(name = "country")
    public String country;

    @Column(name = "place_id")
    public String placeId;

    @Column(name = "occurred_at", nullable = false)
    public Instant occurredAt;
}
