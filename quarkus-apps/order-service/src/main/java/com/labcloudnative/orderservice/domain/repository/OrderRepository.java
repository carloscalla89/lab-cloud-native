package com.labcloudnative.orderservice.domain.repository;

import com.labcloudnative.orderservice.domain.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida (Port) para la persistencia de ordenes.
 *
 * <p>Define el contrato que la capa de aplicacion usa para guardar y
 * recuperar ordenes, expresado unicamente en terminos del dominio. La
 * implementacion concreta (adaptador) vive en la capa de infraestructura,
 * cumpliendo asi la regla de dependencia de la arquitectura limpia: el
 * dominio no conoce los detalles tecnicos (JPA, PostgreSQL, etc.).</p>
 */
public interface OrderRepository {

    /**
     * Persiste una orden nueva o actualiza una existente.
     *
     * @param order la orden a guardar
     * @return la orden persistida
     */
    Order save(Order order);

    /**
     * Busca una orden por su identificador.
     *
     * @param id identificador de la orden
     * @return la orden envuelta en un {@link Optional}, vacio si no existe
     */
    Optional<Order> findById(UUID id);

    /**
     * Recupera todas las ordenes.
     *
     * @return lista de ordenes (posiblemente vacia)
     */
    List<Order> findAll();

    /**
     * Elimina una orden por su identificador.
     *
     * @param id identificador de la orden a eliminar
     * @return {@code true} si se elimino, {@code false} si no existia
     */
    boolean deleteById(UUID id);
}
