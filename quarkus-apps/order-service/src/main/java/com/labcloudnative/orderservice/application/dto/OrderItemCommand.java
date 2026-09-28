package com.labcloudnative.orderservice.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Comando de entrada que representa una linea al crear una orden.
 *
 * <p>Es un objeto de la capa de aplicacion (DTO) usado como contrato de
 * entrada. Las anotaciones de validacion garantizan que los datos sean
 * coherentes antes de llegar al dominio.</p>
 *
 * @param productId   identificador del producto
 * @param productName nombre del producto
 * @param quantity    cantidad solicitada (minimo 1)
 * @param unitPrice   precio unitario (no negativo)
 */
public record OrderItemCommand(

        @NotBlank(message = "El productId es obligatorio")
        String productId,

        @NotBlank(message = "El productName es obligatorio")
        String productName,

        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        int quantity,

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
        BigDecimal unitPrice
) {
}
