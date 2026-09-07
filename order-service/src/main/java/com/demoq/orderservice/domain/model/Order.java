package com.kcd.orderservice.domain.model;

import com.kcd.orderservice.domain.exception.InvalidOrderStateException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Raiz del agregado (Aggregate Root) Orden.
 *
 * <p>Es una entidad de dominio pura (sin anotaciones de JPA ni de ningun
 * framework). Concentra las reglas de negocio y las invariantes: el total
 * siempre refleja la suma de sus lineas y las transiciones de estado solo
 * pueden ocurrir si son validas.</p>
 */
public class Order {

    private final UUID id;
    private final String customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Constructor completo usado para reconstruir una orden desde la
     * persistencia (rehidratacion). No aplica reglas de creacion.
     */
    public Order(UUID id,
                 String customerId,
                 List<OrderItem> items,
                 OrderStatus status,
                 BigDecimal totalAmount,
                 Instant createdAt,
                 Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Metodo de fabrica para crear una nueva orden en estado {@link OrderStatus#CREATED}.
     * Aplica las reglas de negocio de creacion y calcula el total inicial.
     *
     * @param customerId identificador del cliente (obligatorio)
     * @param items      lineas de la orden (al menos una)
     * @return la nueva orden
     * @throws IllegalArgumentException si los datos no son validos
     */
    public static Order create(String customerId, List<OrderItem> items) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("El customerId es obligatorio");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener al menos un item");
        }
        Instant now = Instant.now();
        Order order = new Order(
                UUID.randomUUID(),
                customerId,
                items,
                OrderStatus.CREATED,
                BigDecimal.ZERO,
                now,
                now);
        order.recalculateTotal();
        return order;
    }

    /**
     * Confirma la orden. Solo es valido desde el estado {@link OrderStatus#CREATED}.
     *
     * @throws InvalidOrderStateException si la orden no esta en estado CREATED
     */
    public void confirm() {
        if (this.status != OrderStatus.CREATED) {
            throw new InvalidOrderStateException(
                    "Solo se puede confirmar una orden en estado CREATED. Estado actual: " + this.status);
        }
        this.status = OrderStatus.CONFIRMED;
        touch();
    }

    /**
     * Cancela la orden. No es posible cancelar una orden en estado terminal.
     *
     * @throws InvalidOrderStateException si la orden ya esta en un estado terminal
     */
    public void cancel() {
        if (this.status.isTerminal()) {
            throw new InvalidOrderStateException(
                    "No se puede cancelar una orden en estado terminal: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
        touch();
    }

    /**
     * Recalcula el total sumando el subtotal de todas las lineas.
     */
    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Marca la orden como modificada actualizando la marca de tiempo.
     */
    private void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    /**
     * @return una vista inmutable de las lineas para proteger las invariantes del agregado
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
