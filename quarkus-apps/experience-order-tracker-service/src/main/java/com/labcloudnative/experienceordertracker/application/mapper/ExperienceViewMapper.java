package com.labcloudnative.experienceordertracker.application.mapper;

import com.labcloudnative.experienceordertracker.application.dto.CreateOrderViewCommand;
import com.labcloudnative.experienceordertracker.application.dto.CreateOrderWithDestinationCommand;
import com.labcloudnative.experienceordertracker.application.dto.LocationView;
import com.labcloudnative.experienceordertracker.application.dto.OrderLineView;
import com.labcloudnative.experienceordertracker.application.dto.OrderSummaryView;
import com.labcloudnative.experienceordertracker.application.dto.OrderTrackingView;
import com.labcloudnative.experienceordertracker.application.dto.OrderWithDestinationView;
import com.labcloudnative.experienceordertracker.application.dto.RegisterTrackingViewCommand;
import com.labcloudnative.experienceordertracker.application.dto.TrackingPointView;
import com.labcloudnative.experienceordertracker.application.dto.TrackingView;
import com.labcloudnative.experienceordertracker.domain.model.NewOrderItem;
import com.labcloudnative.experienceordertracker.domain.model.NewOrderRequest;
import com.labcloudnative.experienceordertracker.domain.model.NewTrackingEvent;
import com.labcloudnative.experienceordertracker.domain.model.OrderLine;
import com.labcloudnative.experienceordertracker.domain.model.OrderSummary;
import com.labcloudnative.experienceordertracker.domain.model.OrderTracking;
import com.labcloudnative.experienceordertracker.domain.model.ShipmentTracking;
import com.labcloudnative.experienceordertracker.domain.model.TrackingPoint;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Mapper de la capa de aplicacion.
 *
 * <p>Adapta los modelos de dominio compuestos a los DTOs de vista, que son el
 * payload "a medida" para el frontend. Aqui se materializa la responsabilidad
 * del orquestador: <b>composicion y adaptacion de datos</b>.</p>
 */
@ApplicationScoped
public class ExperienceViewMapper {

    /**
     * Construye la vista compuesta de orden + tracking.
     *
     * @param orderTracking composicion de dominio
     * @return la vista a medida para la pantalla de detalle
     */
    public OrderTrackingView toView(OrderTracking orderTracking) {
        OrderSummary order = orderTracking.order();
        List<OrderLineView> items = order.lines().stream()
                .map(this::toLineView)
                .toList();

        return new OrderTrackingView(
                order.orderId(),
                order.customerId(),
                order.status(),
                order.totalAmount(),
                items,
                toTrackingView(orderTracking.tracking()));
    }

    /**
     * Construye la vista ligera de una orden para el listado.
     *
     * @param order resumen de dominio
     * @return la vista de listado
     */
    public OrderSummaryView toSummaryView(OrderSummary order) {
        return new OrderSummaryView(
                order.orderId(),
                order.customerId(),
                order.status(),
                order.totalAmount(),
                order.lineCount());
    }

    /**
     * Construye la vista del historial de tracking de un envio.
     *
     * @param tracking historial de dominio
     * @return la vista de tracking a medida para el frontend
     */
    public TrackingView toTrackingView(ShipmentTracking tracking) {
        // La ubicacion actual se resuelve a partir del evento mas reciente.
        LocationView currentLocation = tracking.currentLocation()
                .map(point -> new LocationView(point.latitude(), point.longitude(), point.formattedAddress()))
                .orElse(null);

        List<TrackingPointView> history = tracking.events().stream()
                .map(this::toPointView)
                .toList();

        return new TrackingView(
                tracking.shipmentId(),
                tracking.isAvailable(),
                currentLocation,
                history);
    }

    /**
     * Construye la vista de un punto de tracking individual (p.ej. el evento
     * recien registrado).
     *
     * @param point punto de dominio
     * @return la vista del punto de tracking
     */
    public TrackingPointView toPointView(TrackingPoint point) {
        return new TrackingPointView(
                point.latitude(),
                point.longitude(),
                point.formattedAddress(),
                point.city(),
                point.country(),
                point.occurredAt());
    }

    private OrderLineView toLineView(OrderLine line) {
        return new OrderLineView(
                line.productName(),
                line.quantity(),
                line.unitPrice(),
                line.subtotal());
    }

    /**
     * Traduce el comando de entrada de creacion de orden al modelo de dominio.
     *
     * @param command comando recibido por el endpoint REST
     * @return el comando en lenguaje de dominio para el puerto de salida
     */
    public NewOrderRequest toDomain(CreateOrderViewCommand command) {
        List<NewOrderItem> items = command.items().stream()
                .map(item -> new NewOrderItem(
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        item.unitPrice()))
                .toList();
        return new NewOrderRequest(command.customerId(), items);
    }

    /**
     * Traduce el comando de entrada de registro de tracking al modelo de dominio.
     *
     * @param command comando recibido por el endpoint REST
     * @return el comando en lenguaje de dominio para el puerto de salida
     */
    public NewTrackingEvent toDomain(RegisterTrackingViewCommand command) {
        return new NewTrackingEvent(command.shipmentId(), command.latitude(), command.longitude());
    }

    /**
     * Traduce el comando de creacion de orden con destino al modelo de dominio
     * de orden, extrayendo solo los campos relevantes para order-service.
     *
     * @param command comando recibido por el endpoint REST
     * @return el comando de orden en lenguaje de dominio
     */
    public NewOrderRequest toDomain(CreateOrderWithDestinationCommand command) {
        List<NewOrderItem> items = command.items().stream()
                .map(item -> new NewOrderItem(
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        item.unitPrice()))
                .toList();
        return new NewOrderRequest(command.customerId(), items);
    }

    /**
     * Construye la vista de creacion de orden con destino, combinando el resumen
     * de la orden y el punto de tracking del destino.
     *
     * <p>Si {@code destination} es {@code null} (tracking-service no disponible),
     * la vista refleja la degradacion controlada con {@code destinationRegistered = false}.</p>
     *
     * @param order       resumen de la orden recien creada
     * @param destination punto de tracking del destino, o {@code null} si fallo
     * @return la vista combinada
     */
    public OrderWithDestinationView toOrderWithDestinationView(OrderSummary order, TrackingPoint destination) {
        return new OrderWithDestinationView(
                order.orderId(),
                order.customerId(),
                order.status(),
                order.totalAmount(),
                order.lineCount(),
                destination != null,
                destination != null ? toPointView(destination) : null);
    }
}
