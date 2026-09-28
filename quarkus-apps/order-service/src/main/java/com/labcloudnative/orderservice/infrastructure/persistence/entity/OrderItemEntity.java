package com.labcloudnative.orderservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Entidad de persistencia (JPA) para una linea de orden.
 *
 * <p>Es un detalle de infraestructura: mapea la tabla {@code order_items}.
 * Se mantiene separada del objeto de valor {@code OrderItem} del dominio
 * para que el modelo de negocio no dependa de JPA.</p>
 */
@Entity
@Table(name = "order_items")
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    /** Relacion hacia la orden propietaria de esta linea. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    public OrderEntity order;

    @Column(name = "product_id", nullable = false)
    public String productId;

    @Column(name = "product_name", nullable = false)
    public String productName;

    @Column(name = "quantity", nullable = false)
    public int quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    public BigDecimal unitPrice;
}
