package com.kcd.experienceordertracker.infrastructure.rest;

import com.kcd.experienceordertracker.application.dto.RegisterTrackingViewCommand;
import com.kcd.experienceordertracker.application.dto.TrackingPointView;
import com.kcd.experienceordertracker.application.dto.TrackingView;
import com.kcd.experienceordertracker.application.service.OrderExperienceService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Adaptador de entrada REST para el tracking de envios.
 *
 * <p>Es la cara del BFF hacia el frontend para listar y registrar eventos de
 * tracking. Solo traduce HTTP <-> casos de uso y delega toda la orquestacion
 * en {@link OrderExperienceService}.</p>
 */
@Path("/experience/tracking-events")
@Produces(MediaType.APPLICATION_JSON)
public class TrackingExperienceResource {

    private final OrderExperienceService experienceService;

    public TrackingExperienceResource(OrderExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    /**
     * Lista los eventos de tracking.
     *
     * <p>Si se indica {@code shipmentId}, devuelve el historial de ese envio
     * (lista de uno). Si no se indica, devuelve el historial de todos los
     * envios.</p>
     *
     * @param shipmentId identificador del envio (opcional)
     * @return la vista del historial de tracking de cada envio
     */
    @GET
    public List<TrackingView> listTrackingEvents(@QueryParam("shipmentId") String shipmentId) {
        return experienceService.listTrackingEvents(shipmentId);
    }

    /**
     * Registra un evento de tracking a traves de tracking-service.
     *
     * @param command datos del evento a registrar
     * @return 201 con la vista del punto de tracking registrado
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerTrackingEvent(@Valid RegisterTrackingViewCommand command) {
        TrackingPointView created = experienceService.registerTrackingEvent(command);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}
