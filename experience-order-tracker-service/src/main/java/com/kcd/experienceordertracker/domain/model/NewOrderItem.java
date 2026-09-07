package com.kcd.experienceordertracker.domain.model;

import java.math.BigDecimal;

/**
 * Modelo de dominio (comando) de una linea para crear una orden.
 *
 * @param productId   identificador del producto
 * @param productName nombre del producto
 * @param quantity    cantidad
 * @param unitPrice   precio unitario
 */
public record NewOrderItem(
        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
}
