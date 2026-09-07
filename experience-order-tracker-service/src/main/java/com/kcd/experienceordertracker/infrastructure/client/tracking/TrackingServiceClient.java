package com.kcd.experienceordertracker.infrastructure.client.tracking;

import com.kcd.experienceordertracker.infrastructure.filter.ClientRequestResponseLoggingFilter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * Cliente REST tipado para tracking-service.
 *
 * <p>La URL base se configura con la clave {@code tracking-service}
 * (ver {@code quarkus.rest-client.tracking-service.url}).</p>
 */
@RegisterRestClient(configKey = "tracking-service")
@RegisterProvider(ClientRequestResponseLoggingFilter.class)
@Path("/tracking-events")
public interface TrackingServiceClient {

    /**
     * Obtiene el historial de tracking de envíos
     * @return lista de eventos de tracking del envio
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<TrackingEventDto> getAllShipments();

    /**
     * Obtiene el historial de tracking de un envio.
     *
     * @param shipmentId identificador del envio
     * @return lista de eventos de tracking del envio
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<TrackingEventDto> getByShipmentId(@QueryParam("shipmentId") String shipmentId);

    /**
     * Registra un evento de tracking.
     *
     * @param request datos del evento a registrar
     * @return el evento registrado segun tracking-service (con direccion ya geocodificada)
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    TrackingEventDto register(TrackingEventRequestDto request);
}
