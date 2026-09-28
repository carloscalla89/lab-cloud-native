package com.labcloudnative.experienceordertracker.domain.port;

import com.labcloudnative.experienceordertracker.domain.model.NewOrderRequest;
import com.labcloudnative.experienceordertracker.domain.model.OrderSummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida (Port) hacia order-service.
 *
 * <p>Expresa, en lenguaje del dominio del orquestador, las capacidades que se
 * necesitan de order-service. El adaptador concreto (REST client) vive en la
 * capa de infraestructura, de modo que la orquestacion no depende de los
 * detalles de transporte ni del formato del upstream.</p>
 */
public interface OrderServicePort {

    /**
     * Recupera el resumen de una orden por su identificador.
     *
     * @param orderId identificador de la orden
     * @return la orden, o vacio si order-service responde que no existe
     */
    Optional<OrderSummary> findOrderById(UUID orderId);

    /**
     * Recupera el resumen de todas las ordenes.
     *
     * @return lista de ordenes (posiblemente vacia)
     */
    List<OrderSummary> findAllOrders();

    /**
     * Crea una orden en order-service.
     *
     * @param request datos de la orden a crear
     * @return la orden creada
     */
    OrderSummary createOrder(NewOrderRequest request);

    /**
     * Elimina una orden en order-service.
     *
     * @param orderId identificador de la orden a eliminar
     */
    void deleteOrder(UUID orderId);
}
