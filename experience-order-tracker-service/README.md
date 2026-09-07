# experience-order-tracker-service

Microservicio **BFF (Backend for Frontend) / orquestador** en Quarkus
(Java 21, Quarkus 3.33 LTS). Es el **punto de entrada unificado** para las
aplicaciones cliente (web/mobile).

## Responsabilidad

- Recibir la petición del frontend.
- Orquestar llamadas hacia **order-service** y **tracking-service**.
- **Componer y adaptar** los datos en un payload limpio y a medida para la UI.

**No** tiene lógica de negocio pesada ni base de datos propia. Solo composición
y adaptación.

## Arquitectura (Clean Architecture)

```
com.kcd.experienceordertracker
├── domain                      # Modelos de composición + puertos (sin frameworks)
│   ├── model                   # OrderSummary, ShipmentTracking, OrderTracking, ...
│   ├── port                    # OrderServicePort, TrackingServicePort (salida)
│   └── exception               # OrderNotFound, UpstreamService
│
├── application                 # Orquestación + adaptación
│   ├── dto                     # Vistas a medida para el frontend
│   ├── mapper                  # ExperienceViewMapper (dominio -> vista)
│   └── service                 # OrderExperienceService (orquesta y compone)
│
└── infrastructure              # Detalles técnicos (adaptadores)
    ├── client                  # Clientes REST hacia los microservicios
    │   ├── order               # DTOs externos + cliente + adaptador de order-service
    │   └── tracking            # DTOs externos + cliente + adaptador de tracking-service
    └── rest                    # Endpoint BFF + mapeo de excepciones
        └── exception
```

Los DTOs de cada upstream viven aislados en `infrastructure/client/*` y se
traducen a modelos de dominio propios; así, un cambio en order-service o
tracking-service no se propaga por todo el servicio.

## Cómo orquesta

`GET /experience/orders/{orderId}`:

1. Pide la orden a **order-service**. Si no existe → `404`.
2. Pide el tracking a **tracking-service** usando el `orderId` como `shipmentId`.
3. Compone ambos y devuelve un único payload a medida.

> **Convención:** se asume `shipmentId == orderId`. Si la orden incorpora un
> `shipmentId` propio, solo se cambia esa correlación en `OrderExperienceService`.

### Resiliencia y degradación controlada

- La orden es **obligatoria**: si order-service falla → `502`.
- El tracking es **complementario**: si tracking-service falla, la vista se
  devuelve igualmente con `tracking.available = false` (no rompe la UI).
- Las llamadas a los upstreams usan `@Timeout` y `@Retry` (MicroProfile Fault
  Tolerance) para tolerar fallos transitorios.

## Requisitos

- JDK 21+
- Maven 3.9+
- **order-service** corriendo en `:8080` y **tracking-service** en `:8081`

## Configuración

| Variable               | Descripción                         | Default                  |
|------------------------|-------------------------------------|--------------------------|
| `ORDER_SERVICE_URL`    | URL base de order-service           | `http://localhost:8080`  |
| `TRACKING_SERVICE_URL` | URL base de tracking-service        | `http://localhost:8081`  |

## Ejecución

```bash
cd experience-order-tracker-service
mvn quarkus:dev
```

- API:        http://localhost:8082/experience/orders
- Swagger UI: http://localhost:8082/q/swagger-ui
- Health:     http://localhost:8082/q/health

## Endpoints

| Método | Ruta                                | Descripción                                |
|--------|-------------------------------------|--------------------------------------------|
| GET    | `/experience/orders`                | Listado ligero de órdenes                  |
| GET    | `/experience/orders/{orderId}`      | Detalle de orden + tracking (composición)  |

### Ejemplo de respuesta (`GET /experience/orders/{orderId}`)

```json
{
  "orderId": "11111111-1111-1111-1111-111111111111",
  "customerId": "cust-1001",
  "status": "CONFIRMED",
  "totalAmount": 66.00,
  "items": [
    { "productName": "Teclado Mecanico", "quantity": 2, "unitPrice": 25.50, "subtotal": 51.00 }
  ],
  "tracking": {
    "shipmentId": "11111111-1111-1111-1111-111111111111",
    "available": true,
    "currentLocation": {
      "latitude": -12.046374,
      "longitude": -77.042793,
      "address": "Av. Ejemplo 123, Lima, Perú"
    },
    "history": [
      {
        "latitude": -12.046374,
        "longitude": -77.042793,
        "address": "Av. Ejemplo 123, Lima, Perú",
        "city": "Lima",
        "country": "Perú",
        "occurredAt": "2026-06-18T10:15:30Z"
      }
    ]
  }
}
```

## Códigos de error

| Situación                              | HTTP |
|----------------------------------------|------|
| Orden no encontrada                    | 404  |
| Fallo de un upstream requerido         | 502  |

## Observabilidad (OpenTelemetry)

El servicio exporta **traces, métricas y logs** por **OTLP** mediante la
extensión `quarkus-opentelemetry`. El destino se configura con:

| Variable | Descripción | Default |
|----------|-------------|---------|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | Endpoint del OpenTelemetry Collector | `http://localhost:4317` |

En modo `dev` (`mvn quarkus:dev`) la exportación está **desactivada**
(`%dev.quarkus.otel.sdk.disabled=true`) para no requerir un collector local. El
`service.name` de la telemetría es `experience-order-tracker-service`
(de `quarkus.application.name`). Como BFF, sus trazas **encadenan** con las de
order-service y tracking-service (traza distribuida end-to-end).

## Despliegue en Kubernetes

Los manifiestos y scripts viven en [`../../k8s`](../../k8s). El despliegue de
este servicio se automatiza con
`k8s/scripts/deploy-experience-order-tracker-service.sh` (build, `kind load` y
`kubectl apply`). Ver [`../../k8s/README.md`](../../k8s/README.md).
