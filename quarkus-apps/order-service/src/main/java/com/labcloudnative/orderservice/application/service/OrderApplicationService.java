package com.labcloudnative.orderservice.application.service;

import com.labcloudnative.orderservice.application.dto.CreateOrderCommand;
import com.labcloudnative.orderservice.application.dto.OrderResponse;
import com.labcloudnative.orderservice.application.mapper.OrderDtoMapper;
import com.labcloudnative.orderservice.domain.exception.OrderNotFoundException;
import com.labcloudnative.orderservice.domain.model.Order;
import com.labcloudnative.orderservice.domain.model.OrderItem;
import com.labcloudnative.orderservice.domain.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicacion: orquesta los casos de uso de la gestion de ordenes.
 *
 * <p>Coordina el modelo de dominio y el puerto de persistencia
 * ({@link OrderRepository}), pero no contiene reglas de negocio (esas viven
 * en el dominio). Define los limites transaccionales de cada operacion.</p>
 *
 * <p>Depende de la abstraccion {@link OrderRepository}, no de su
 * implementacion concreta, respetando la inversion de dependencias.</p>
 */
@ApplicationScoped
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderDtoMapper mapper;

    /**
     * Constructor con inyeccion de dependencias (CDI).
     *
     * @param orderRepository puerto de persistencia de ordenes
     * @param mapper          mapper entre DTOs y dominio
     */
    public OrderApplicationService(OrderRepository orderRepository, OrderDtoMapper mapper) {
        this.orderRepository = orderRepository;
        this.mapper = mapper;
    }

    /**
     * Caso de uso: crear una nueva orden.
     *
     * @param command datos de la orden a crear
     * @return la orden creada
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderCommand command) {
        List<OrderItem> items = mapper.toDomain(command.items());
        Order order = Order.create(command.customerId(), items);
        Order saved = orderRepository.save(order);
        return mapper.toResponse(saved);
    }

    /**
     * Caso de uso: obtener una orden por su identificador.
     *
     * @param id identificador de la orden
     * @return la orden encontrada
     * @throws OrderNotFoundException si la orden no existe
     */
    public OrderResponse getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return mapper.toResponse(order);
    }

    /**
     * Caso de uso: listar todas las ordenes.
     *
     * @return lista de ordenes
     */
    public List<OrderResponse> listOrders() {
        return orderRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Caso de uso: confirmar una orden.
     *
     * @param id identificador de la orden
     * @return la orden actualizada
     * @throws OrderNotFoundException si la orden no existe
     */
    @Transactional
    public OrderResponse confirmOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        order.confirm();
        Order saved = orderRepository.save(order);
        return mapper.toResponse(saved);
    }

    /**
     * Caso de uso: cancelar una orden.
     *
     * @param id identificador de la orden
     * @return la orden actualizada
     * @throws OrderNotFoundException si la orden no existe
     */
    @Transactional
    public OrderResponse cancelOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        order.cancel();
        Order saved = orderRepository.save(order);
        return mapper.toResponse(saved);
    }

    /**
     * Caso de uso: eliminar una orden.
     *
     * @param id identificador de la orden
     * @throws OrderNotFoundException si la orden no existe
     */
    @Transactional
    public void deleteOrder(UUID id) {
        boolean deleted = orderRepository.deleteById(id);
        if (!deleted) {
            throw new OrderNotFoundException(id);
        }
    }
}
