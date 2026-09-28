package com.labcloudnative.trackingservice.infrastructure.persistence;

import com.labcloudnative.trackingservice.domain.model.TrackingEvent;
import com.labcloudnative.trackingservice.domain.repository.TrackingEventRepository;
import com.labcloudnative.trackingservice.infrastructure.persistence.entity.TrackingEventEntity;
import com.labcloudnative.trackingservice.infrastructure.persistence.mapper.TrackingEventEntityMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia: implementacion concreta del puerto
 * {@link TrackingEventRepository}.
 *
 * <p>Conecta el contrato del dominio con Hibernate ORM + Panache + PostgreSQL,
 * traduciendo entre dominio y entidades mediante
 * {@link TrackingEventEntityMapper}. Define la transaccion en la operacion de
 * escritura para mantener la llamada externa de geocodificacion fuera de ella.</p>
 */
@ApplicationScoped
public class TrackingEventRepositoryAdapter implements TrackingEventRepository {

    private final TrackingEventPanacheRepository panacheRepository;
    private final TrackingEventEntityMapper mapper;

    public TrackingEventRepositoryAdapter(TrackingEventPanacheRepository panacheRepository,
                                          TrackingEventEntityMapper mapper) {
        this.panacheRepository = panacheRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TrackingEvent save(TrackingEvent event) {
        TrackingEventEntity entity = mapper.toEntity(event);
        panacheRepository.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<TrackingEvent> findById(UUID id) {
        return panacheRepository.findByIdOptional(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<TrackingEvent> findByShipmentId(String shipmentId) {
        return panacheRepository.findByShipmentId(shipmentId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<TrackingEvent> findAllShipments() {
        return panacheRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
