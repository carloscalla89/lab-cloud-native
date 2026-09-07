package com.kcd.orderservice.infrastructure.persistence;

import com.kcd.orderservice.infrastructure.persistence.entity.OrderEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

/**
 * Repositorio Panache de bajo nivel para {@link OrderEntity}.
 *
 * <p>Provee las operaciones CRUD basicas sobre la entidad de persistencia.
 * Es un detalle de infraestructura que el adaptador
 * {@code OrderRepositoryAdapter} utiliza internamente.</p>
 */
@ApplicationScoped
public class OrderPanacheRepository implements PanacheRepositoryBase<OrderEntity, UUID> {
    // Hereda findById, persist, listAll, deleteById, etc. de Panache.
    // No se agregan consultas personalizadas por ahora.
}
