-- ======================================================================
-- V100: Datos de prueba (SOLO entorno dev)
-- ======================================================================
-- Esta migracion vive en la ubicacion db/dev, que unicamente se carga en
-- el perfil 'dev' (ver %dev.quarkus.flyway.locations en application.properties).
-- Asi nunca se ejecuta en produccion.
--
-- Se usan UUIDs e identificadores fijos + ON CONFLICT DO NOTHING para que
-- la insercion sea idempotente y no falle si los datos ya existen.
-- ======================================================================

-- ---------------------------------------------------------------------
-- Orden 1: estado CREATED (cliente cust-1001)
-- ---------------------------------------------------------------------
INSERT INTO orders (id, customer_id, status, total_amount, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'cust-1001', 'CREATED', 66.00,
        TIMESTAMPTZ '2026-01-10 09:00:00+00', TIMESTAMPTZ '2026-01-10 09:00:00+00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'p-100', 'Teclado Mecanico',   2, 25.50),
    ('11111111-1111-1111-1111-111111111111', 'p-101', 'Mouse Inalambrico',  1, 15.00);

-- ---------------------------------------------------------------------
-- Orden 2: estado CONFIRMED (cliente cust-1002)
-- ---------------------------------------------------------------------
INSERT INTO orders (id, customer_id, status, total_amount, created_at, updated_at)
VALUES ('22222222-2222-2222-2222-222222222222', 'cust-1002', 'CONFIRMED', 222.49,
        TIMESTAMPTZ '2026-02-15 14:30:00+00', TIMESTAMPTZ '2026-02-15 15:00:00+00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price)
VALUES
    ('22222222-2222-2222-2222-222222222222', 'p-200', 'Monitor 27 pulgadas', 1, 199.99),
    ('22222222-2222-2222-2222-222222222222', 'p-201', 'Cable HDMI',          3,   7.50);

-- ---------------------------------------------------------------------
-- Orden 3: estado CANCELLED (cliente cust-1003)
-- ---------------------------------------------------------------------
INSERT INTO orders (id, customer_id, status, total_amount, created_at, updated_at)
VALUES ('33333333-3333-3333-3333-333333333333', 'cust-1003', 'CANCELLED', 45.00,
        TIMESTAMPTZ '2026-03-01 11:00:00+00', TIMESTAMPTZ '2026-03-01 12:00:00+00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price)
VALUES
    ('33333333-3333-3333-3333-333333333333', 'p-300', 'Webcam HD', 1, 45.00);
