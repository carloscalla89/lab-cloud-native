package com.labcloudnative.experienceordertracker.domain.model;

import java.math.BigDecimal;

/**
 * Modelo de dominio (Value Object) de una linea de orden, en la
 * representacion propia del orquestador.
 *
 * <p>Es independiente del formato que expone order-service: el adaptador del
 * cliente traduce el DTO externo a este tipo, evitando que cambios en el
 * upstream se propaguen al resto del servicio.</p>
 *
 * @param productName nombre del producto
 * @param quantity    cantidad
 * @param unitPrice   precio unitario
 * @param subtotal    importe de la linea
 */
public record OrderLine(
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
