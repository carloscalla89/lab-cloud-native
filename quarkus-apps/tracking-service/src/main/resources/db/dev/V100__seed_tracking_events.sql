-- ======================================================================
-- V100: Datos de ejemplo de tracking (SOLO entorno dev)
-- ======================================================================
-- Esta migracion vive en la ubicacion db/dev, que unicamente se carga en
-- el perfil 'dev' (ver %dev.quarkus.flyway.locations en application.properties).
-- Asi el seed nunca se ejecuta en produccion.
--
-- Los shipment_id coinciden con los orderId del seed de order-service
-- (db/dev/V100__seed_test_data.sql) porque el BFF asume shipmentId == orderId.
-- Se usan UUIDs fijos + ON CONFLICT DO NOTHING para que la insercion sea
-- idempotente y no falle si los eventos ya existen.
--
-- Coordenadas reales de Lima / Callao (Peru) para que el historial tenga un
-- recorrido coherente.
-- ======================================================================

-- ---------------------------------------------------------------------
-- Envio 1: orderId 11111111-1111-1111-1111-111111111111 (cust-1001)
-- Recorrido: Av. Ejemplo -> Av. Arequipa -> Av. Javier Prado Este
-- ---------------------------------------------------------------------
INSERT INTO tracking_events
    (id, shipment_id, latitude, longitude, formatted_address, street, city, postal_code, country, place_id, occurred_at)
VALUES
    ('aaaaaaaa-0000-0000-0000-000000000001',
     '11111111-1111-1111-1111-111111111111',
     -12.046374, -77.042793,
     'Av. Ejemplo 123, Lima, Peru', 'Av. Ejemplo 123', 'Lima', '15001', 'Peru', 'demo-place-0001',
     TIMESTAMPTZ '2026-01-10 09:05:00+00'),
    ('aaaaaaaa-0000-0000-0000-000000000002',
     '11111111-1111-1111-1111-111111111111',
     -12.056500, -77.035200,
     'Av. Arequipa 2500, Lima, Peru', 'Av. Arequipa 2500', 'Lima', '15046', 'Peru', 'demo-place-0002',
     TIMESTAMPTZ '2026-01-10 09:30:00+00'),
    ('aaaaaaaa-0000-0000-0000-000000000003',
     '11111111-1111-1111-1111-111111111111',
     -12.091100, -77.023400,
     'Av. Javier Prado Este 4200, Lima, Peru', 'Av. Javier Prado Este 4200', 'Lima', '15076', 'Peru', 'demo-place-0003',
     TIMESTAMPTZ '2026-01-10 10:15:00+00')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------
-- Envio 2: orderId 22222222-2222-2222-2222-222222222222 (cust-1002)
-- Recorrido: Miraflores -> Callao -> Centro de Lima
-- ---------------------------------------------------------------------
INSERT INTO tracking_events
    (id, shipment_id, latitude, longitude, formatted_address, street, city, postal_code, country, place_id, occurred_at)
VALUES
    ('bbbbbbbb-0000-0000-0000-000000000001',
     '22222222-2222-2222-2222-222222222222',
     -12.121900, -77.029600,
     'Av. Larco 345, Miraflores, Lima, Peru', 'Av. Larco 345', 'Miraflores', '15074', 'Peru', 'demo-place-0011',
     TIMESTAMPTZ '2026-02-15 14:35:00+00'),
    ('bbbbbbbb-0000-0000-0000-000000000002',
     '22222222-2222-2222-2222-222222222222',
     -12.056900, -77.118500,
     'Av. Elmer Faucett, Callao, Peru', 'Av. Elmer Faucett', 'Callao', '07001', 'Peru', 'demo-place-0012',
     TIMESTAMPTZ '2026-02-15 15:10:00+00'),
    ('bbbbbbbb-0000-0000-0000-000000000003',
     '22222222-2222-2222-2222-222222222222',
     -12.046000, -77.042000,
     'Jr. de la Union 300, Lima, Peru', 'Jr. de la Union 300', 'Lima', '15001', 'Peru', 'demo-place-0013',
     TIMESTAMPTZ '2026-02-15 16:00:00+00')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------
-- Envio 3: orderId 33333333-3333-3333-3333-333333333333 (cust-1003)
-- Recorrido: San Isidro -> Surco -> La Molina
-- ---------------------------------------------------------------------
INSERT INTO tracking_events
    (id, shipment_id, latitude, longitude, formatted_address, street, city, postal_code, country, place_id, occurred_at)
VALUES
    ('cccccccc-0000-0000-0000-000000000001',
     '33333333-3333-3333-3333-333333333333',
     -12.097800, -77.036500,
     'Av. Pardo y Aliaga 640, San Isidro, Lima, Peru', 'Av. Pardo y Aliaga 640', 'San Isidro', '15073', 'Peru', 'demo-place-0021',
     TIMESTAMPTZ '2026-03-01 11:05:00+00'),
    ('cccccccc-0000-0000-0000-000000000002',
     '33333333-3333-3333-3333-333333333333',
     -12.145600, -76.994600,
     'Av. Benavides 5000, Santiago de Surco, Lima, Peru', 'Av. Benavides 5000', 'Santiago de Surco', '15023', 'Peru', 'demo-place-0022',
     TIMESTAMPTZ '2026-03-01 11:45:00+00'),
    ('cccccccc-0000-0000-0000-000000000003',
     '33333333-3333-3333-3333-333333333333',
     -12.079400, -76.944700,
     'Av. Raul Ferrero 1200, La Molina, Lima, Peru', 'Av. Raul Ferrero 1200', 'La Molina', '15024', 'Peru', 'demo-place-0023',
     TIMESTAMPTZ '2026-03-01 12:20:00+00')
ON CONFLICT (id) DO NOTHING;
