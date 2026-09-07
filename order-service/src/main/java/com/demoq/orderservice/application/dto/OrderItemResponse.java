package com.kcd.orderservice.application.dto;

import java.math.BigDecimal;

/**
 * DTO de salida que representa una linea de orden en las respuestas de la API.
 *
 * @param productId   identificador del producto
 * @param productName nombre del producto
 * @param quantity    cantidad
 * @param unitPrice   precio unitario
 * @param subtotal    importe de la linea (precio unitario * cantidad)
 */
public record OrderItemResponse(
        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
