package com.labcloudnative.orderservice.infrastructure.rest;

import com.labcloudnative.orderservice.application.dto.CreateOrderCommand;
import com.labcloudnative.orderservice.application.dto.OrderResponse;
import com.labcloudnative.orderservice.application.service.OrderApplicationService;
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
 * Adaptador de entrada REST: expone los casos de uso de ordenes via HTTP.
 *
 * <p>Es la frontera externa del microservicio. Solo se encarga de la
 * traduccion HTTP <-> casos de uso; delega toda la logica en
 * {@link OrderApplicationService}. No contiene reglas de negocio.</p>
 */
@Path("/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    private final OrderApplicationService orderService;

    public OrderResource(OrderApplicationService orderService) {
        this.orderService = orderService;
    }

    /**
     * Crea una nueva orden.
     *
     * @param command datos de la orden (validados)
     * @return 201 Created con la orden creada y la cabecera Location
     */
    @POST
    public Response create(@Valid CreateOrderCommand command) {
        OrderResponse created = orderService.createOrder(command);
        return Response.created(URI.create("/orders/" + created.id()))
                .entity(created)
                .build();
    }

    /**
     * Lista todas las ordenes.
     *
     * @return 200 OK con la lista de ordenes
     */
    @GET
    public List<OrderResponse> list() {
        return orderService.listOrders();
    }

    /**
     * Obtiene una orden por su identificador.
     *
     * @param id identificador de la orden
     * @return 200 OK con la orden (o 404 si no existe)
     */
    @GET
    @Path("/{id}")
    public OrderResponse get(@PathParam("id") UUID id) {
        return orderService.getOrder(id);
    }

    /**
     * Confirma una orden.
     *
     * @param id identificador de la orden
     * @return 200 OK con la orden actualizada
     */
    @POST
    @Path("/{id}/confirm")
    public OrderResponse confirm(@PathParam("id") UUID id) {
        return orderService.confirmOrder(id);
    }

    /**
     * Cancela una orden.
     *
     * @param id identificador de la orden
     * @return 200 OK con la orden actualizada
     */
    @POST
    @Path("/{id}/cancel")
    public OrderResponse cancel(@PathParam("id") UUID id) {
        return orderService.cancelOrder(id);
    }

    /**
     * Elimina una orden.
     *
     * @param id identificador de la orden
     * @return 204 No Content (o 404 si no existe)
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        orderService.deleteOrder(id);
        return Response.noContent().build();
    }
}
