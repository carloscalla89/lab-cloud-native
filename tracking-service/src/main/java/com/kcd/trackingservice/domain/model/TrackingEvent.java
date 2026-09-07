package com.kcd.trackingservice.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Raiz del agregado (Aggregate Root) del subdominio de envios: un evento de
 * tracking.
 *
 * <p>Registra que, en un instante dado, un envio ({@code shipmentId}) estuvo
 * en unas {@link Coordinates} que se resolvieron a una {@link Address}
 * mediante geocodificacion inversa. Es una entidad de dominio pura, sin
 * dependencias de frameworks.</p>
 */
public class TrackingEvent {

    private final UUID id;
    private final String shipmentId;
    private final Coordinates coordinates;
    private final Address address;
    private final Instant occurredAt;

    /**
     * Constructor completo para rehidratar el evento desde la persistencia.
     */
    public TrackingEvent(UUID id,
                         String shipmentId,
                         Coordinates coordinates,
                         Address address,
                         Instant occurredAt) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.coordinates = coordinates;
        this.address = address;
        this.occurredAt = occurredAt;
    }

    /**
     * Metodo de fabrica para registrar un nuevo evento de tracking.
     * Genera el identificador y fija el instante de ocurrencia.
     *
     * @param shipmentId  identificador del envio (obligatorio)
     * @param coordinates coordenadas del evento
     * @param address     direccion resuelta por geocodificacion inversa
     * @return el nuevo evento de tracking
     * @throws IllegalArgumentException si los datos son invalidos
     */
    public static TrackingEvent register(String shipmentId, Coordinates coordinates, Address address) {
        if (shipmentId == null || shipmentId.isBlank()) {
            throw new IllegalArgumentException("El shipmentId es obligatorio");
        }
        if (coordinates == null) {
            throw new IllegalArgumentException("Las coordenadas son obligatorias");
        }
        if (address == null) {
            throw new IllegalArgumentException("La direccion es obligatoria");
        }
        return new TrackingEvent(UUID.randomUUID(), shipmentId, coordinates, address, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public Address getAddress() {
        return address;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
