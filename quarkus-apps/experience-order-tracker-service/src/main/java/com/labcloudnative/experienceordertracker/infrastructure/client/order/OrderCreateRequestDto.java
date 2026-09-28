package com.labcloudnative.experienceordertracker.infrastructure.client.order;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO externo: representa el cuerpo de la peticion de creacion de orden tal
 * como lo espera order-service.
 *
 * <p>Modela el contrato "tal cual" del upstream y vive exclusivamente en la
 * capa de infraestructura.</p>
 */
public record OrderCreateRequestDto(
        String customerId,
        List<OrderItemRequestDto> items
) {

    /** DTO externo de una linea de la orden a crear segun order-service. */
    public record OrderItemRequestDto(
            String productId,
            String productName,
            int quantity,
            BigDecimal unitPrice
    ) {
    }
}
