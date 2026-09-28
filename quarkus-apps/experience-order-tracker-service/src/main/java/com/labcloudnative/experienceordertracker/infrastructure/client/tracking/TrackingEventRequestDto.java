package com.labcloudnative.experienceordertracker.infrastructure.client.tracking;

/**
 * DTO externo: representa el cuerpo de la peticion de registro de un evento
 * de tracking tal como lo espera tracking-service.
 *
 * <p>Modela el contrato "tal cual" del upstream y vive exclusivamente en la
 * capa de infraestructura.</p>
 */
public record TrackingEventRequestDto(
        String shipmentId,
        Double latitude,
        Double longitude
) {
}
