package com.kcd.experienceordertracker.application.dto;

import java.math.BigDecimal;

/**
 * DTO de salida (vista) de una linea de orden, adaptado para el frontend.
 *
 * @param productName nombre del producto
 * @param quantity    cantidad
 * @param unitPrice   precio unitario
 * @param subtotal    importe de la linea
 */
public record OrderLineView(
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
