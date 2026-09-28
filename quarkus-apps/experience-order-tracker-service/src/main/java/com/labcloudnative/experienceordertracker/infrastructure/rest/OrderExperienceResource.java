package com.labcloudnative.experienceordertracker.infrastructure.rest;

import com.labcloudnative.experienceordertracker.application.dto.CreateOrderViewCommand;
import com.labcloudnative.experienceordertracker.application.dto.CreateOrderWithDestinationCommand;
import com.labcloudnative.experienceordertracker.application.dto.OrderSummaryView;
import com.labcloudnative.experienceordertracker.application.dto.OrderTrackingView;
import com.labcloudnative.experienceordertracker.application.dto.OrderWithDestinationView;
import com.labcloudnative.experienceordertracker.application.service.OrderExperienceService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador de entrada REST: punto de entrada unificado para las aplicaciones
 * cliente (web/mobile).
 *
 * <p>Es la cara del BFF hacia el frontend. Solo traduce HTTP <-> casos de uso
 * y delega toda la orquestacion en {@link OrderExperienceService}.</p>
 */
@Path("/experience/orders")
@Produces(MediaType.APPLICATION_JSON)
public class OrderExperienceResource {

    private final OrderExperienceService experienceService;

    public OrderExperienceResource(OrderExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    /**
     * Devuelve el detalle de una orden con su tracking, compuesto y adaptado
     * para el frontend (pantalla de detalle/seguimiento).
     *
     * @param orderId identificador de la orden
     * @return la vista compuesta orden + tracking
     */
    @GET
    @Path("/{orderId}")
    public OrderTrackingView getOrderWithTracking(@PathParam("orderId") UUID orderId) {
        return experienceService.getOrderWithTracking(orderId);
    }

    /**
     * Devuelve el listado ligero de ordenes para la pantalla de listado.
     *
     * @return lista de vistas resumidas de orden
     */
    @GET
    public List<OrderSummaryView> listOrders() {
        return experienceService.listOrders();
    }

    /**
     * Crea una orden a traves de order-service.
     *
     * @param command datos de la orden a crear
     * @return 201 con la vista resumida de la orden creada
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrder(@Valid CreateOrderViewCommand command) {
        OrderSummaryView created = experienceService.createOrder(command);
        return Response.created(URI.create("/experience/orders/" + created.orderId()))
                .entity(created)
                .build();
    }

    /**
     * Crea una orden con su tracking de destino en una sola llamada al BFF.
     *
     * <p>El BFF orquesta de forma secuencial:
     * <ol>
     *   <li>Creacion de la orden en order-service.</li>
     *   <li>Registro del evento de tracking en tracking-service con las coordenadas
     *       del destino y el orderId como shipmentId.</li>
     * </ol>
     * Si tracking-service no responde, la respuesta incluye {@code destinationRegistered: false}
     * pero la orden ya existe (degradacion controlada).</p>
     *
     * @param command datos de la orden y coordenadas de entrega
     * @return 201 con la vista combinada: resumen de orden + destino geocodificado
     */
    @POST
    @Path("/with-destination")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrderWithDestination(@Valid CreateOrderWithDestinationCommand command) {
        OrderWithDestinationView created = experienceService.createOrderWithDestination(command);
        return Response.created(URI.create("/experience/orders/" + created.orderId()))
                .entity(created)
                .build();
    }

    /**
     * Elimina una orden a traves de order-service.
     *
     * @param orderId identificador de la orden a eliminar
     * @return 204 sin contenido
     */
    @DELETE
    @Path("/{orderId}")
    public Response deleteOrder(@PathParam("orderId") UUID orderId) {
        experienceService.deleteOrder(orderId);
        return Response.noContent().build();
    }
}
