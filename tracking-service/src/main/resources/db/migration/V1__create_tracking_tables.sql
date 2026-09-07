-- ======================================================================
-- V1: Creacion del esquema inicial de eventos de tracking
-- ======================================================================

-- Cada fila representa un evento de tracking de un envio en un momento dado,
-- con sus coordenadas y la direccion resuelta por geocodificacion inversa.
CREATE TABLE tracking_events (
    id                UUID            NOT NULL,
    shipment_id       VARCHAR(255)    NOT NULL,
    latitude          DOUBLE PRECISION NOT NULL,
    longitude         DOUBLE PRECISION NOT NULL,
    formatted_address VARCHAR(512)    NOT NULL,
    street            VARCHAR(255),
    city              VARCHAR(255),
    postal_code       VARCHAR(64),
    country           VARCHAR(255),
    place_id          VARCHAR(255),
    occurred_at       TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_tracking_events PRIMARY KEY (id)
);

-- Indice para recuperar el historial de tracking de un envio.
CREATE INDEX idx_tracking_events_shipment_id ON tracking_events (shipment_id);
