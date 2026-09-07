package com.kcd.orderservice.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Objeto de valor (Value Object) que representa una linea de una orden.
 *
 * <p>Es inmutable: una vez creado no cambia. Su identidad se define por el
 * conjunto de sus atributos, no por un identificador. Encapsula las
 * invariantes de negocio de una linea de pedido (cantidad positiva y precio
 * no negativo).</p>
 */
public final class OrderItem {

    private final String productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;

    /**
     * Crea una linea de orden validando sus invariantes.
     *
     * @param productId   identificador del producto (obligatorio)
     * @param productName nombre descriptivo del producto (obligatorio)
     * @param quantity    cantidad solicitada, debe ser mayor que cero
     * @param unitPrice   precio unitario, no puede ser negativo
     * @throws IllegalArgumentException si alguna invariante no se cumple
     */
    public OrderItem(String productId, String productName, int quantity, BigDecimal unitPrice) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("El productId es obligatorio");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("El productName es obligatorio");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /**
     * Calcula el subtotal de la linea (precio unitario * cantidad).
     *
     * @return el importe de la linea
     */
    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderItem that)) {
            return false;
        }
        return quantity == that.quantity
                && Objects.equals(productId, that.productId)
                && Objects.equals(productName, that.productName)
                && unitPrice.compareTo(that.unitPrice) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, quantity, unitPrice.stripTrailingZeros());
    }
}
