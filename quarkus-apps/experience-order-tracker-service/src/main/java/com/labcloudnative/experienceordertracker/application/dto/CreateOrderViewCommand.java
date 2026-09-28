package com.labcloudnative.experienceordertracker.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO de entrada (comando) para crear una orden a traves del BFF.
 *
 * @param customerId identificador del cliente
 * @param items      lineas de la orden (al menos una)
 */
public record CreateOrderViewCommand(
        @NotBlank String customerId,
        @NotEmpty @Valid List<OrderItemViewCommand> items
) {
}
