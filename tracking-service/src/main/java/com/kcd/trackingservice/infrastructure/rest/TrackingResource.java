package com.kcd.trackingservice.infrastructure.rest;

import com.kcd.trackingservice.application.dto.RegisterTrackingCommand;
import com.kcd.trackingservice.application.dto.TrackingEventResponse;
import com.kcd.trackingservice.application.service.TrackingApplicationService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador de entrada REST: expone los casos de uso de tracking via HTTP.
 *
 * <p>Es la frontera externa del microservicio. Solo traduce HTTP <-> casos de
 * uso y delega toda la logica en {@link TrackingApplicationService}.</p>
 */
@Path("/tracking-events")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TrackingResource {

    private final TrackingApplicationService trackingService;

    public TrackingResource(TrackingApplicationService trackingService) {
        this.trackingService = trackingService;
    }

    /**
     * Registra un nuevo evento de tracking: recibe coordenadas, hace
     * geocodificacion inversa y persiste el evento.
     *
     * @param command datos del evento (validados)
     * @return 201 Created con el evento registrado y la cabecera Location
     */
    @POST
    public Response register(@Valid RegisterTrackingCommand command) {
        TrackingEventResponse created = trackingService.register(command);
        return Response.created(URI.create("/tracking-events/" + created.id()))
                .entity(created)
                .build();
    }

    /**
     * Obtiene un evento de tracking por su identificador.
     *
     * @param id identificador del evento
     * @return 200 OK con el evento (o 404 si no existe)
     */
    @GET
    @Path("/{id}")
    public TrackingEventResponse getById(@PathParam("id") UUID id) {
        return trackingService.getById(id);
    }

    /**
     * Lista el historial de eventos de un envio.
     *
     * @param shipmentId identificador del envio (parametro de consulta obligatorio)
     * @return 200 OK con la lista de eventos del envio
     */
    @GET
    public List<TrackingEventResponse> getByShipment(@QueryParam("shipmentId") String shipmentId) {
        return trackingService.getByShipment(shipmentId);
    }
}
