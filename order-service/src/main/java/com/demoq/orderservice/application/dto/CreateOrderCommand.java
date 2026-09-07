package com.kcd.orderservice.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Comando de entrada para el caso de uso "crear orden".
 *
 * <p>Modela los datos que el cliente envia. Pertenece a la capa de
 * aplicacion y desacopla la API publica del modelo de dominio.</p>
 *
 * @param customerId identificador del cliente
 * @param items      lineas de la orden (al menos una)
 */
public record CreateOrderCommand(

        @NotBlank(message = "El customerId es obligatorio")
        String customerId,

        @NotEmpty(message = "La orden debe tener al menos un item")
        @Valid
        List<OrderItemCommand> items
) {
}
