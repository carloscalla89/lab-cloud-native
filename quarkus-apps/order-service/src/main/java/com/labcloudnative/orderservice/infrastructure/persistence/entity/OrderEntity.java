package com.labcloudnative.orderservice.infrastructure.persistence.entity;

import com.labcloudnative.orderservice.domain.model.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad de persistencia (JPA) para una orden.
 *
 * <p>Mapea la tabla {@code orders}. Es un detalle de infraestructura,
 * deliberadamente separado de la entidad de dominio {@code Order} para no
 * contaminar el nucleo de negocio con anotaciones de JPA.</p>
 */
@Entity
@Table(name = "orders")
public class OrderEntity {

    /**
     * Identificador asignado por el dominio (UUID generado en la creacion),
     * por eso no usa una estrategia de generacion automatica.
     */
    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "customer_id", nullable = false)
    public String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    public OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    public BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    /**
     * Lineas de la orden. Cascada total y orphanRemoval para que el ciclo de
     * vida de las lineas siga al de la orden.
     */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    public List<OrderItemEntity> items = new ArrayList<>();

    /**
     * Metodo de conveniencia para mantener la relacion bidireccional
     * consistente al agregar una linea.
     *
     * @param item linea a asociar a esta orden
     */
    public void addItem(OrderItemEntity item) {
        item.order = this;
        this.items.add(item);
    }
}
