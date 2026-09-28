package com.labcloudnative.trackingservice.application.service;

import com.labcloudnative.trackingservice.application.dto.RegisterTrackingCommand;
import com.labcloudnative.trackingservice.application.dto.TrackingEventResponse;
import com.labcloudnative.trackingservice.application.mapper.TrackingDtoMapper;
import com.labcloudnative.trackingservice.domain.exception.TrackingEventNotFoundException;
import com.labcloudnative.trackingservice.domain.model.Address;
import com.labcloudnative.trackingservice.domain.model.Coordinates;
import com.labcloudnative.trackingservice.domain.model.TrackingEvent;
import com.labcloudnative.trackingservice.domain.port.ReverseGeocodingPort;
import com.labcloudnative.trackingservice.domain.repository.TrackingEventRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicacion: orquesta los casos de uso del subdominio de envios.
 *
 * <p>Coordina el puerto de geocodificacion ({@link ReverseGeocodingPort}, la
 * frontera del ACL) y el puerto de persistencia
 * ({@link TrackingEventRepository}). No contiene reglas de negocio: estas
 * residen en el dominio.</p>
 *
 * <p>Importante: la llamada a la API externa se realiza <b>fuera</b> de la
 * transaccion de base de datos. La transaccionalidad se delega en el
 * adaptador de persistencia ({@code save}), de modo que la conexion a la base
 * de datos no quede retenida mientras se espera la respuesta HTTP del
 * proveedor externo.</p>
 */
@ApplicationScoped
public class TrackingApplicationService {

    private final ReverseGeocodingPort reverseGeocodingPort;
    private final TrackingEventRepository trackingEventRepository;
    private final TrackingDtoMapper mapper;

    public TrackingApplicationService(ReverseGeocodingPort reverseGeocodingPort,
                                      TrackingEventRepository trackingEventRepository,
                                      TrackingDtoMapper mapper) {
        this.reverseGeocodingPort = reverseGeocodingPort;
        this.trackingEventRepository = trackingEventRepository;
        this.mapper = mapper;
    }

    /**
     * Caso de uso: registrar un evento de tracking.
     *
     * <p>Flujo: valida las coordenadas, consulta la API externa para obtener
     * la direccion (geocodificacion inversa), crea el evento de dominio y lo
     * persiste.</p>
     *
     * @param command datos del evento (envio + coordenadas)
     * @return el evento registrado
     */
    public TrackingEventResponse register(RegisterTrackingCommand command) {
        // 1) Construir y validar las coordenadas (invariantes del dominio).
        Coordinates coordinates = new Coordinates(command.latitude(), command.longitude());

        // 2) Geocodificacion inversa a traves del ACL (llamada externa, sin transaccion).
        Address address = reverseGeocodingPort.reverseGeocode(coordinates);

        // 3) Registrar el evento de dominio y persistirlo (transaccion en el adaptador).
        TrackingEvent event = TrackingEvent.register(command.shipmentId(), coordinates, address);
        TrackingEvent saved = trackingEventRepository.save(event);

        return mapper.toResponse(saved);
    }

    /**
     * Caso de uso: obtener un evento de tracking por su identificador.
     *
     * @param id identificador del evento
     * @return el evento encontrado
     * @throws TrackingEventNotFoundException si no existe
     */
    public TrackingEventResponse getById(UUID id) {
        TrackingEvent event = trackingEventRepository.findById(id)
                .orElseThrow(() -> new TrackingEventNotFoundException(id));
        return mapper.toResponse(event);
    }

    /**
     * Caso de uso: obtener el historial de tracking de un envio.
     *
     * @param shipmentId identificador del envio
     * @return lista de eventos del envio
     */
    public List<TrackingEventResponse> getByShipment(String shipmentId) {

        if (shipmentId != null) {
            return trackingEventRepository.findByShipmentId(shipmentId).stream()
                    .map(mapper::toResponse)
                    .toList();
        } else {
            return trackingEventRepository.findAllShipments().stream()
                    .map(mapper::toResponse)
                    .toList();
        }


    }

}
