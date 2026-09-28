package com.labcloudnative.experienceordertracker.infrastructure.client.order;

import com.labcloudnative.experienceordertracker.infrastructure.filter.ClientRequestResponseLoggingFilter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;
import java.util.UUID;

/**
 * Cliente REST tipado para order-service.
 *
 * <p>La URL base se configura con la clave {@code order-service}
 * (ver {@code quarkus.rest-client.order-service.url}).</p>
 */
@RegisterRestClient(configKey = "order-service")
@RegisterProvider(ClientRequestResponseLoggingFilter.class)
@Path("/orders")
public interface OrderServiceClient {

    /**
     * Obtiene una orden por su identificador. Devuelve 404 si no existe.
     *
     * @param id identificador de la orden
     * @return la orden segun order-service
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    OrderDto getById(@PathParam("id") UUID id);

    /**
     * Lista todas las ordenes.
     *
     * @return lista de ordenes segun order-service
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrderDto> listAll();

    /**
     * Crea una orden.
     *
     * @param request datos de la orden a crear
     * @return la orden creada segun order-service
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    OrderDto create(OrderCreateRequestDto request);

    /**
     * Elimina una orden.
     *
     * @param id identificador de la orden
     */
    @DELETE
    @Path("/{id}")
    void delete(@PathParam("id") UUID id);
}
