package com.kcd.experienceordertracker.domain.port;

import com.kcd.experienceordertracker.domain.model.NewTrackingEvent;
import com.kcd.experienceordertracker.domain.model.ShipmentTracking;
import com.kcd.experienceordertracker.domain.model.TrackingPoint;

import java.util.List;

/**
 * Puerto de salida (Port) hacia tracking-service.
 *
 * <p>Expresa la capacidad de obtener el historial de tracking de un envio. El
 * adaptador concreto (REST client) reside en infraestructura.</p>
 */
public interface TrackingServicePort {

    /**
     * Recupera el historial de tracking de envíos
     *

     * @return el historial de tracking (eventos del mas reciente al mas antiguo)
     */
    List<ShipmentTracking> findAllTrackingEvents();

    /**
     * Recupera el historial de tracking de un envio.
     *
     * @param shipmentId identificador del envio
     * @return el historial de tracking (eventos del mas reciente al mas antiguo)
     */
    ShipmentTracking findTrackingByShipmentId(String shipmentId);

    /**
     * Registra un evento de tracking en tracking-service.
     *
     * @param request datos del evento a registrar
     * @return el punto de tracking registrado (con direccion ya geocodificada)
     */
    TrackingPoint registerTrackingEvent(NewTrackingEvent request);
}
