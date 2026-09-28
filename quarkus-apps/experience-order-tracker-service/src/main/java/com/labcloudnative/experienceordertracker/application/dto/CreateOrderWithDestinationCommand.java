package com.labcloudnative.experienceordertracker.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO de entrada (comando) para crear una orden con su tracking de destino
 * en una sola llamada al BFF.
 *
 * <p>El BFF orquesta dos operaciones en secuencia:
 * <ol>
 *   <li>Crea la orden en order-service.</li>
 *   <li>Registra el evento de tracking de destino en tracking-service usando
 *       el orderId como shipmentId (convencion del sistema).</li>
 * </ol>
 * Si tracking-service no esta disponible, la orden se crea igualmente y la
 * respuesta indica que el destino no pudo registrarse (degradacion controlada).</p>
 *
 * @param customerId           identificador del cliente
 * @param items                lineas de la orden (al menos una)
 * @param destinationLatitude  latitud de la direccion de entrega
 * @param destinationLongitude longitud de la direccion de entrega
 */
public record CreateOrderWithDestinationCommand(
        @NotBlank String customerId,
        @NotEmpty @Valid List<OrderItemViewCommand> items,
        @NotNull @DecimalMin("-90.0")  @DecimalMax("90.0")  Double destinationLatitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double destinationLongitude
) {
}
