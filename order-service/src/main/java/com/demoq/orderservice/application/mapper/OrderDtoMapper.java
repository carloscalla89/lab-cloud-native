package com.kcd.orderservice.application.mapper;

import com.kcd.orderservice.application.dto.OrderItemCommand;
import com.kcd.orderservice.application.dto.OrderItemResponse;
import com.kcd.orderservice.application.dto.OrderResponse;
import com.kcd.orderservice.domain.model.Order;
import com.kcd.orderservice.domain.model.OrderItem;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Mapper de la capa de aplicacion.
 *
 * <p>Traduce entre los DTOs (contratos de entrada/salida) y el modelo de
 * dominio. Aisla al dominio del formato de la API y viceversa.</p>
 */
@ApplicationScoped
public class OrderDtoMapper {

    /**
     * Convierte un comando de linea en un objeto de valor del dominio.
     *
     * @param command comando de linea recibido
     * @return la linea de dominio correspondiente
     */
    public OrderItem toDomain(OrderItemCommand command) {
        return new OrderItem(
                command.productId(),
                command.productName(),
                command.quantity(),
                command.unitPrice());
    }

    /**
     * Convierte la lista de comandos de linea en objetos de valor del dominio.
     *
     * @param commands lista de comandos de linea
     * @return lista de lineas de dominio
     */
    public List<OrderItem> toDomain(List<OrderItemCommand> commands) {
        return commands.stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * Convierte una orden de dominio en su DTO de respuesta.
     *
     * @param order orden de dominio
     * @return DTO de salida con la representacion publica de la orden
     */
    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    /**
     * Convierte una linea de dominio en su DTO de respuesta, incluyendo el subtotal.
     *
     * @param item linea de dominio
     * @return DTO de salida de la linea
     */
    public OrderItemResponse toResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.subtotal());
    }
}
