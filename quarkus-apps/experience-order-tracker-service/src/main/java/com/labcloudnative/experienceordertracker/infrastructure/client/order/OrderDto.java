package com.labcloudnative.experienceordertracker.infrastructure.client.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO externo: representa la respuesta de order-service para una orden.
 *
 * <p>Modela el contrato "tal cual" del upstream y vive exclusivamente en la
 * capa de infraestructura. El adaptador lo traduce al modelo de dominio
 * {@code OrderSummary}, evitando que cambios de order-service se propaguen.</p>
 *
 * <p>{@code @JsonIgnoreProperties(ignoreUnknown = true)} hace el parseo
 * tolerante a campos adicionales del upstream (p.ej. createdAt/updatedAt).</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderDto(
        UUID id,
        String customerId,
        String status,
        BigDecimal totalAmount,
        List<OrderItemDto> items
) {

    /** DTO externo de una linea de la orden segun order-service. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderItemDto(
            String productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }
}
