package com.labcloudnative.experienceordertracker.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO de entrada (comando) de una linea para crear una orden.
 *
 * @param productId   identificador del producto
 * @param productName nombre del producto
 * @param quantity    cantidad (minimo 1)
 * @param unitPrice   precio unitario (no negativo)
 */
public record OrderItemViewCommand(
        @NotBlank String productId,
        @NotBlank String productName,
        @Min(1) int quantity,
        @NotNull @DecimalMin("0.0") BigDecimal unitPrice
) {
}
