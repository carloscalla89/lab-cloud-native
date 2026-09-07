package com.kcd.experienceordertracker.application.service;

import com.kcd.experienceordertracker.application.dto.CreateOrderViewCommand;
import com.kcd.experienceordertracker.application.dto.CreateOrderWithDestinationCommand;
import com.kcd.experienceordertracker.application.dto.OrderSummaryView;
import com.kcd.experienceordertracker.application.dto.OrderTrackingView;
import com.kcd.experienceordertracker.application.dto.OrderWithDestinationView;
import com.kcd.experienceordertracker.application.dto.RegisterTrackingViewCommand;
import com.kcd.experienceordertracker.application.dto.TrackingPointView;
import com.kcd.experienceordertracker.application.dto.TrackingView;
import com.kcd.experienceordertracker.application.mapper.ExperienceViewMapper;
import com.kcd.experienceordertracker.domain.exception.OrderNotFoundException;
import com.kcd.experienceordertracker.domain.exception.UpstreamServiceException;
import com.kcd.experienceordertracker.domain.model.NewTrackingEvent;
import com.kcd.experienceordertracker.domain.model.OrderSummary;
import com.kcd.experienceordertracker.domain.model.OrderTracking;
import com.kcd.experienceordertracker.domain.model.ShipmentTracking;
import com.kcd.experienceordertracker.domain.model.TrackingPoint;
import com.kcd.experienceordertracker.domain.port.OrderServicePort;
import com.kcd.experienceordertracker.domain.port.TrackingServicePort;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicacion: orquesta order-service y tracking-service y compone
 * el resultado.
 *
 * <p>Es el nucleo del BFF. No contiene logica de negocio pesada ni accede a
 * ninguna base de datos: su trabajo es coordinar llamadas a los puertos,
 * combinar los resultados y delegar la adaptacion al
 * {@link ExperienceViewMapper}.</p>
 *
 * <p><b>Convencion:</b> se asume que el {@code shipmentId} del envio coincide
 * con el identificador de la orden. Si en el futuro la orden incluye un
 * shipmentId propio, basta con cambiar esta correlacion.</p>
 */
@ApplicationScoped
public class OrderExperienceService {

    private final OrderServicePort orderServicePort;
    private final TrackingServicePort trackingServicePort;
    private final ExperienceViewMapper mapper;

    public OrderExperienceService(OrderServicePort orderServicePort,
                                  TrackingServicePort trackingServicePort,
                                  ExperienceViewMapper mapper) {
        this.orderServicePort = orderServicePort;
        this.trackingServicePort = trackingServicePort;
        this.mapper = mapper;
    }

    /**
     * Caso de uso principal: obtener una orden junto con su tracking, ya
     * compuesto y adaptado para el frontend.
     *
     * <p>La orden es obligatoria (si no existe -> 404). El tracking es
     * complementario: si tracking-service falla, se degrada de forma controlada
     * devolviendo la orden con un tracking vacio, para no romper la experiencia.</p>
     *
     * @param orderId identificador de la orden
     * @return la vista compuesta orden + tracking
     * @throws OrderNotFoundException   si la orden no existe
     * @throws UpstreamServiceException si order-service (requerido) falla
     */
    public OrderTrackingView getOrderWithTracking(UUID orderId) {
        // 1) La orden es el dato obligatorio de la composicion.
        OrderSummary order = orderServicePort.findOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 2) El tracking es complementario: se obtiene con degradacion controlada.
        String shipmentId = orderId.toString();
        ShipmentTracking tracking;
        try {
            tracking = trackingServicePort.findTrackingByShipmentId(shipmentId);
        } catch (UpstreamServiceException e) {
            Log.warnf(e, "tracking-service no disponible para el envio %s; se devuelve la orden sin tracking", shipmentId);
            tracking = ShipmentTracking.empty(shipmentId);
        }

        // 3) Composicion + adaptacion al payload del frontend.
        return mapper.toView(new OrderTracking(order, tracking));
    }

    /**
     * Caso de uso: listar las ordenes en formato ligero para la pantalla de
     * listado del frontend (sin tracking).
     *
     * @return lista de vistas resumidas de orden
     */
    public List<OrderSummaryView> listOrders() {
        return orderServicePort.findAllOrders().stream()
                .map(mapper::toSummaryView)
                .toList();
    }

    /**
     * Caso de uso: crear una orden a traves de order-service.
     *
     * @param command datos de la orden a crear
     * @return la vista resumida de la orden creada
     * @throws UpstreamServiceException si order-service falla
     */
    public OrderSummaryView createOrder(CreateOrderViewCommand command) {
        OrderSummary created = orderServicePort.createOrder(mapper.toDomain(command));
        return mapper.toSummaryView(created);
    }

    /**
     * Caso de uso: listar los eventos de tracking.
     *
     * <p>Si se indica {@code shipmentId}, devuelve el historial de ese envio
     * (lista de uno). Si no se indica, devuelve el historial de todos los
     * envios conocidos por tracking-service.</p>
     *
     * @param shipmentId identificador del envio, o {@code null} para listar todos
     * @return la vista del historial de tracking de cada envio
     * @throws UpstreamServiceException si tracking-service falla
     */
    public List<TrackingView> listTrackingEvents(String shipmentId) {
        if (shipmentId == null || shipmentId.isBlank()) {
            return trackingServicePort.findAllTrackingEvents().stream()
                    .map(mapper::toTrackingView)
                    .toList();
        }

        ShipmentTracking tracking = trackingServicePort.findTrackingByShipmentId(shipmentId);
        return List.of(mapper.toTrackingView(tracking));
    }

    /**
     * Caso de uso: crear una orden con su tracking de destino en una sola llamada.
     *
     * <p>Orquesta dos operaciones en secuencia:
     * <ol>
     *   <li>Crea la orden en order-service (obligatorio: si falla, se propaga la excepcion).</li>
     *   <li>Registra el evento de tracking de destino en tracking-service usando el orderId
     *       como shipmentId (degradacion controlada: si falla, la orden ya existe y la
     *       respuesta indica que el destino no pudo registrarse).</li>
     * </ol>
     * </p>
     *
     * @param command datos de la orden y coordenadas del destino
     * @return la vista combinada de la orden creada y su destino geocodificado
     * @throws UpstreamServiceException si order-service (obligatorio) falla
     */
    public OrderWithDestinationView createOrderWithDestination(CreateOrderWithDestinationCommand command) {
        // 1) Crear la orden — es el dato obligatorio; cualquier fallo se propaga.
        OrderSummary order = orderServicePort.createOrder(mapper.toDomain(command));

        // 2) Registrar el destino de envio — complementario; se degrada si tracking-service falla.
        String shipmentId = order.orderId().toString();
        TrackingPoint destination = null;
        try {
            destination = trackingServicePort.registerTrackingEvent(
                    new NewTrackingEvent(shipmentId, command.destinationLatitude(), command.destinationLongitude()));
        } catch (UpstreamServiceException e) {
            Log.warnf(e, "tracking-service no disponible; la orden %s se creo sin destino registrado", shipmentId);
        }

        return mapper.toOrderWithDestinationView(order, destination);
    }

    /**
     * Caso de uso: eliminar una orden a traves de order-service.
     *
     * @param orderId identificador de la orden a eliminar
     * @throws OrderNotFoundException   si la orden no existe
     * @throws UpstreamServiceException si order-service falla
     */
    public void deleteOrder(UUID orderId) {
        orderServicePort.deleteOrder(orderId);
    }

    /**
     * Caso de uso: registrar un evento de tracking a traves de tracking-service.
     *
     * @param command datos del evento a registrar
     * @return la vista del punto de tracking registrado
     * @throws UpstreamServiceException si tracking-service falla
     */
    public TrackingPointView registerTrackingEvent(RegisterTrackingViewCommand command) {
        TrackingPoint registered = trackingServicePort.registerTrackingEvent(mapper.toDomain(command));
        return mapper.toPointView(registered);
    }
}
