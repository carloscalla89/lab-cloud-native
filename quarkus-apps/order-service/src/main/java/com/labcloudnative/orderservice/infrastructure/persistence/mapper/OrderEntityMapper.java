package com.labcloudnative.orderservice.infrastructure.persistence.mapper;

import com.labcloudnative.orderservice.domain.model.Order;
import com.labcloudnative.orderservice.domain.model.OrderItem;
import com.labcloudnative.orderservice.infrastructure.persistence.entity.OrderEntity;
import com.labcloudnative.orderservice.infrastructure.persistence.entity.OrderItemEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Mapper de persistencia.
 *
 * <p>Traduce entre el modelo de dominio ({@link Order}/{@link OrderItem}) y
 * las entidades JPA ({@link OrderEntity}/{@link OrderItemEntity}). Aisla al
 * dominio de los detalles del ORM.</p>
 */
@ApplicationScoped
public class OrderEntityMapper {

    /**
     * Construye una entidad JPA nueva a partir de una orden de dominio.
     *
     * @param order orden de dominio
     * @return entidad lista para persistir
     */
    public OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.id = order.getId();
        entity.customerId = order.getCustomerId();
        entity.status = order.getStatus();
        entity.totalAmount = order.getTotalAmount();
        entity.createdAt = order.getCreatedAt();
        entity.updatedAt = order.getUpdatedAt();

        // Se construyen las lineas y se mantiene la relacion bidireccional.
        for (OrderItem item : order.getItems()) {
            OrderItemEntity itemEntity = new OrderItemEntity();
            itemEntity.productId = item.getProductId();
            itemEntity.productName = item.getProductName();
            itemEntity.quantity = item.getQuantity();
            itemEntity.unitPrice = item.getUnitPrice();
            entity.addItem(itemEntity);
        }
        return entity;
    }

    /**
     * Actualiza los campos mutables de una entidad existente a partir del
     * estado actual del dominio. Las lineas no se modifican porque, segun las
     * reglas de negocio, son inmutables tras la creacion de la orden.
     *
     * @param entity entidad gestionada a actualizar
     * @param order  orden de dominio con el estado mas reciente
     */
    public void updateEntity(OrderEntity entity, Order order) {
        entity.status = order.getStatus();
        entity.totalAmount = order.getTotalAmount();
        entity.updatedAt = order.getUpdatedAt();
    }

    /**
     * Reconstruye una orden de dominio a partir de su entidad JPA.
     *
     * @param entity entidad recuperada de la base de datos
     * @return la orden de dominio rehidratada
     */
    public Order toDomain(OrderEntity entity) {
        List<OrderItem> items = entity.items.stream()
                .map(this::toDomain)
                .toList();

        return new Order(
                entity.id,
                entity.customerId,
                items,
                entity.status,
                entity.totalAmount,
                entity.createdAt,
                entity.updatedAt);
    }

    /**
     * Convierte una entidad de linea en su objeto de valor de dominio.
     *
     * @param entity entidad de linea
     * @return la linea de dominio
     */
    private OrderItem toDomain(OrderItemEntity entity) {
        return new OrderItem(
                entity.productId,
                entity.productName,
                entity.quantity,
                entity.unitPrice);
    }
}
