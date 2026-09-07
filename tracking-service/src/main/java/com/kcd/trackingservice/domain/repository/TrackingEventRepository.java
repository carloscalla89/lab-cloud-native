package com.kcd.trackingservice.domain.repository;

import com.kcd.trackingservice.domain.model.TrackingEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida (Port) para la persistencia de eventos de tracking.
 *
 * <p>Expresa el contrato de persistencia unicamente en terminos del dominio.
 * La implementacion concreta (adaptador con JPA/PostgreSQL) reside en la capa
 * de infraestructura, respetando la regla de dependencia.</p>
 */
public interface TrackingEventRepository {

    /**
     * Persiste un nuevo evento de tracking.
     *
     * @param event evento a guardar
     * @return el evento persistido
     */
    TrackingEvent save(TrackingEvent event);

    /**
     * Busca un evento por su identificador.
     *
     * @param id identificador del evento
     * @return el evento, o vacio si no existe
     */
    Optional<TrackingEvent> findById(UUID id);

    /**
     * Recupera el historial de eventos de un envio por su identificador
     *
     * @param shipmentId identificador del envio
     * @return lista de eventos (posiblemente vacia)
     */
    List<TrackingEvent> findByShipmentId(String shipmentId);

    /**
     * Recupera el historial de eventos de un envio, del mas reciente al mas antiguo.
     *
     * @return lista de eventos (posiblemente vacia)
     */
    List<TrackingEvent> findAllShipments();
}
