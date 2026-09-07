package com.kcd.orderservice.domain.model;

/**
 * Estados posibles del ciclo de vida de una orden.
 *
 * <p>Forma parte del nucleo del dominio: no depende de ningun framework
 * y modela las reglas de negocio referidas a las transiciones validas.</p>
 */
public enum OrderStatus {

    /** Orden recien creada, aun no confirmada. */
    CREATED,

    /** Orden confirmada por el cliente. */
    CONFIRMED,

    /** Orden cancelada; estado terminal. */
    CANCELLED;

    /**
     * Indica si este estado es terminal (no admite mas transiciones).
     *
     * @return {@code true} si la orden no puede cambiar de estado.
     */
    public boolean isTerminal() {
        return this == CANCELLED;
    }
}
