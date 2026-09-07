package com.kcd.orderservice.infrastructure.persistence;

import com.kcd.orderservice.domain.model.Order;
import com.kcd.orderservice.domain.repository.OrderRepository;
import com.kcd.orderservice.infrastructure.persistence.entity.OrderEntity;
import com.kcd.orderservice.infrastructure.persistence.mapper.OrderEntityMapper;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia: implementacion concreta del puerto
 * {@link OrderRepository}.
 *
 * <p>Conecta el contrato del dominio con la tecnologia concreta (Hibernate
 * ORM + Panache + PostgreSQL). Traduce entre dominio y entidades mediante
 * {@link OrderEntityMapper}, de modo que las capas superiores nunca ven una
 * entidad JPA.</p>
 */
@ApplicationScoped
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderPanacheRepository panacheRepository;
    private final OrderEntityMapper mapper;

    public OrderRepositoryAdapter(OrderPanacheRepository panacheRepository, OrderEntityMapper mapper) {
        this.panacheRepository = panacheRepository;
        this.mapper = mapper;
    }

    /**
     * Persiste una orden nueva o actualiza una existente.
     *
     * <p>Si la orden ya existe, se actualizan sus campos mutables sobre la
     * entidad gestionada; si no, se crea y persiste una entidad nueva.</p>
     */
    @Override
    public Order save(Order order) {
        OrderEntity existing = panacheRepository.findById(order.getId());
        if (existing == null) {
            OrderEntity entity = mapper.toEntity(order);
            panacheRepository.persist(entity);
            return mapper.toDomain(entity);
        }
        mapper.updateEntity(existing, order);
        // Al estar gestionada, los cambios se sincronizan al cerrar la transaccion.
        return mapper.toDomain(existing);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return panacheRepository.findByIdOptional(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return panacheRepository.listAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean deleteById(UUID id) {
        return panacheRepository.deleteById(id);
    }
}
