package com.labcloudnative.trackingservice.infrastructure.persistence;

import com.labcloudnative.trackingservice.infrastructure.persistence.entity.TrackingEventEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Panache de bajo nivel para {@link TrackingEventEntity}.
 *
 * <p>Provee las operaciones de acceso a datos sobre la entidad de
 * persistencia. Es un detalle de infraestructura usado por el adaptador
 * {@code TrackingEventRepositoryAdapter}.</p>
 */
@ApplicationScoped
public class TrackingEventPanacheRepository implements PanacheRepositoryBase<TrackingEventEntity, UUID> {

    /**
     * Recupera los eventos de un envio ordenados del mas reciente al mas antiguo.
     *
     * @param shipmentId identificador del envio
     * @return lista de entidades de evento
     */
    public List<TrackingEventEntity> findByShipmentId(String shipmentId) {
        return list("shipmentId", Sort.by("occurredAt").descending(), shipmentId);
    }

}
