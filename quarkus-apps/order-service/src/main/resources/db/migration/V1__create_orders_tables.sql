-- ======================================================================
-- V1: Creacion del esquema inicial de ordenes
-- ======================================================================

-- Tabla principal de ordenes.
CREATE TABLE orders (
    id            UUID            NOT NULL,
    customer_id   VARCHAR(255)    NOT NULL,
    status        VARCHAR(32)     NOT NULL,
    total_amount  NUMERIC(19, 2)  NOT NULL,
    created_at    TIMESTAMPTZ     NOT NULL,
    updated_at    TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

-- Tabla de lineas/items de cada orden.
CREATE TABLE order_items (
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    order_id     UUID            NOT NULL,
    product_id   VARCHAR(255)    NOT NULL,
    product_name VARCHAR(255)    NOT NULL,
    quantity     INTEGER         NOT NULL,
    unit_price   NUMERIC(19, 2)  NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES orders (id) ON DELETE CASCADE
);

-- Indice para acelerar las consultas de items por orden.
CREATE INDEX idx_order_items_order_id ON order_items (order_id);

-- Indice para consultar ordenes por cliente.
CREATE INDEX idx_orders_customer_id ON orders (customer_id);
