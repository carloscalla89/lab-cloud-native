package com.labcloudnative.experienceordertracker.infrastructure.client.tracking;

import com.labcloudnative.experienceordertracker.domain.exception.UpstreamServiceException;
import com.labcloudnative.experienceordertracker.domain.model.NewTrackingEvent;
import com.labcloudnative.experienceordertracker.domain.model.ShipmentTracking;
import com.labcloudnative.experienceordertracker.domain.model.TrackingPoint;
import com.labcloudnative.experienceordertracker.domain.port.TrackingServicePort;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador de salida: implementacion del puerto {@link TrackingServicePort}
 * usando el cliente REST de tracking-service.
 *
 * <p>Traduce los DTOs externos ({@link TrackingEventDto}) al modelo de dominio
 * ({@link TrackingPoint}/{@link ShipmentTracking}) y convierte los fallos de
 * transporte en {@link UpstreamServiceException}. La decision de degradar la
 * experiencia ante esos fallos se toma en la capa de aplicacion.</p>
 *
 * <p>Aplica resiliencia con {@code @Timeout} y {@code @Retry}.</p>
 */
@ApplicationScoped
public class TrackingServiceAdapter implements TrackingServicePort {

    private final TrackingServiceClient client;

    public TrackingServiceAdapter(@RestClient TrackingServiceClient client) {
        this.client = client;
    }

    @Override
    @Timeout(4000)
    @Retry(maxRetries = 2, delay = 300)
    public List<ShipmentTracking> findAllTrackingEvents() {
        try {
            List<TrackingEventDto> events = client.getAllShipments();
            return events.stream()
                    .collect(Collectors.groupingBy(TrackingEventDto::shipmentId))
                    .entrySet().stream()
                    .map(entry -> new ShipmentTracking(
                            entry.getKey(),
                            entry.getValue().stream().map(this::toDomain).toList()))
                    .toList();
        } catch (RuntimeException e) {
            throw new UpstreamServiceException("tracking-service no respondio al listar los eventos de tracking", e);
        }
    }

    @Override
    @Timeout(4000)
    @Retry(maxRetries = 2, delay = 300)
    public ShipmentTracking findTrackingByShipmentId(String shipmentId) {
        try {
            List<TrackingEventDto> events = client.getByShipmentId(shipmentId);
            List<TrackingPoint> points = events.stream()
                    .map(this::toDomain)
                    .toList();
            return new ShipmentTracking(shipmentId, points);
        } catch (RuntimeException e) {
            throw new UpstreamServiceException("tracking-service no respondio para el envio " + shipmentId, e);
        }
    }

    @Override
    @Timeout(4000)
    // Sin @Retry: reintentar un registro (POST, no idempotente) podria
    // duplicar el evento si el primer intento tuvo exito pero se perdio la respuesta.
    public TrackingPoint registerTrackingEvent(NewTrackingEvent request) {
        try {
            TrackingEventRequestDto dto = new TrackingEventRequestDto(
                    request.shipmentId(), request.latitude(), request.longitude());
            TrackingEventDto created = client.register(dto);
            return toDomain(created);
        } catch (RuntimeException e) {
            throw new UpstreamServiceException(
                    "tracking-service fallo al registrar el evento para el envio " + request.shipmentId(), e);
        }
    }

    /**
     * Traduce el DTO externo de tracking-service al modelo de dominio,
     * aplanando los datos de direccion relevantes para el frontend.
     */
    private TrackingPoint toDomain(TrackingEventDto dto) {
        String formattedAddress = null;
        String city = null;
        String country = null;
        if (dto.address() != null) {
            formattedAddress = dto.address().formattedAddress();
            city = dto.address().city();
            country = dto.address().country();
        }

        return new TrackingPoint(
                dto.latitude(),
                dto.longitude(),
                formattedAddress,
                city,
                country,
                dto.occurredAt());
    }
}
