package com.labcloudnative.experienceordertracker.infrastructure.client.order;

import com.labcloudnative.experienceordertracker.domain.exception.OrderNotFoundException;
import com.labcloudnative.experienceordertracker.domain.exception.UpstreamServiceException;
import com.labcloudnative.experienceordertracker.domain.model.NewOrderItem;
import com.labcloudnative.experienceordertracker.domain.model.NewOrderRequest;
import com.labcloudnative.experienceordertracker.domain.model.OrderLine;
import com.labcloudnative.experienceordertracker.domain.model.OrderSummary;
import com.labcloudnative.experienceordertracker.domain.port.OrderServicePort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida: implementacion del puerto {@link OrderServicePort}
 * usando el cliente REST de order-service.
 *
 * <p>Traduce los DTOs externos ({@link OrderDto}) al modelo de dominio y
 * convierte los fallos de transporte en {@link UpstreamServiceException}. Un
 * 404 no es un error de transporte: se interpreta como "orden inexistente"
 * (Optional vacio).</p>
 *
 * <p>Aplica resiliencia con {@code @Timeout} y {@code @Retry} para tolerar
 * fallos transitorios del upstream.</p>
 */
@ApplicationScoped
public class OrderServiceAdapter implements OrderServicePort {

    private final OrderServiceClient client;

    public OrderServiceAdapter(@RestClient OrderServiceClient client) {
        this.client = client;
    }

    @Override
    @Timeout(4000)
    @Retry(maxRetries = 2, delay = 300)
    public Optional<OrderSummary> findOrderById(UUID orderId) {
        try {
            OrderDto dto = client.getById(orderId);
            return Optional.of(toDomain(dto));
        } catch (WebApplicationException e) {
            // 404 => la orden no existe; no es un fallo del upstream.
            if (e.getResponse() != null && e.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                return Optional.empty();
            }
            throw new UpstreamServiceException("order-service fallo al obtener la orden " + orderId, e);
        } catch (RuntimeException e) {
            throw new UpstreamServiceException("order-service no respondio al obtener la orden " + orderId, e);
        }
    }

    @Override
    @Timeout(4000)
    @Retry(maxRetries = 2, delay = 300)
    public List<OrderSummary> findAllOrders() {
        try {
            return client.listAll().stream()
                    .map(this::toDomain)
                    .toList();
        } catch (RuntimeException e) {
            throw new UpstreamServiceException("order-service no respondio al listar las ordenes", e);
        }
    }

    @Override
    @Timeout(4000)
    // Sin @Retry: reintentar una creacion (POST, no idempotente) podria
    // duplicar la orden si el primer intento tuvo exito pero se perdio la respuesta.
    public OrderSummary createOrder(NewOrderRequest request) {
        try {
            OrderDto created = client.create(toRequestDto(request));
            return toDomain(created);
        } catch (RuntimeException e) {
            throw new UpstreamServiceException("order-service fallo al crear la orden", e);
        }
    }

    @Override
    @Timeout(4000)
    // Sin @Retry: un DELETE reintentado tras una respuesta perdida encontraria
    // la orden ya eliminada y reportaria un 404 enganoso en vez de exito.
    public void deleteOrder(UUID orderId) {
        try {
            client.delete(orderId);
        } catch (WebApplicationException e) {
            if (e.getResponse() != null && e.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new OrderNotFoundException(orderId);
            }
            throw new UpstreamServiceException("order-service fallo al eliminar la orden " + orderId, e);
        } catch (RuntimeException e) {
            throw new UpstreamServiceException("order-service no respondio al eliminar la orden " + orderId, e);
        }
    }

    /**
     * Traduce el DTO externo de order-service al modelo de dominio del orquestador.
     */
    private OrderSummary toDomain(OrderDto dto) {
        List<OrderLine> lines = dto.items() == null ? List.of() : dto.items().stream()
                .map(item -> new OrderLine(
                        item.productName(),
                        item.quantity(),
                        item.unitPrice(),
                        item.subtotal()))
                .toList();

        return new OrderSummary(
                dto.id(),
                dto.customerId(),
                dto.status(),
                dto.totalAmount(),
                lines);
    }

    /**
     * Traduce el comando de dominio al DTO de peticion externo de order-service.
     */
    private OrderCreateRequestDto toRequestDto(NewOrderRequest request) {
        List<OrderCreateRequestDto.OrderItemRequestDto> items = request.items().stream()
                .map(this::toRequestItemDto)
                .toList();
        return new OrderCreateRequestDto(request.customerId(), items);
    }

    private OrderCreateRequestDto.OrderItemRequestDto toRequestItemDto(NewOrderItem item) {
        return new OrderCreateRequestDto.OrderItemRequestDto(
                item.productId(),
                item.productName(),
                item.quantity(),
                item.unitPrice());
    }
}
