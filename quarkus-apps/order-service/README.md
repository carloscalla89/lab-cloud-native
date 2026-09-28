# order-service

Microservicio en **Quarkus** cuya única responsabilidad es **gestionar el estado
y la persistencia de las órdenes** en **PostgreSQL**.

## Arquitectura (Clean Architecture)

El código se organiza en tres capas con la regla de dependencia apuntando
siempre hacia el dominio (de afuera hacia adentro):

```
com.labcloudnative.orderservice
├── domain                 # Núcleo de negocio (sin frameworks)
│   ├── model              # Order (aggregate root), OrderItem (value object), OrderStatus
│   ├── exception          # Excepciones de negocio (OrderNotFound, InvalidOrderState)
│   └── repository         # Puerto de persistencia (OrderRepository)
│
├── application            # Casos de uso / orquestación
│   ├── dto                # Comandos de entrada y DTOs de salida
│   ├── mapper             # Traducción DTO <-> dominio
│   └── service            # OrderApplicationService (límites transaccionales)
│
└── infrastructure         # Detalles técnicos (adaptadores)
    ├── persistence        # Entidades JPA, repositorio Panache, adaptador del puerto
    │   ├── entity
    │   └── mapper
    └── rest               # Adaptador de entrada HTTP + mapeo de excepciones
        └── exception
```

- **domain** no depende de nadie. No conoce JPA, REST ni Quarkus.
- **application** depende solo del **domain** (usa el puerto `OrderRepository`).
- **infrastructure** depende de **application** y **domain**, e implementa los
  puertos (inversión de dependencias).

## Requisitos

- JDK 21+
- Maven 3.9+
- Quarkus 3.33 (LTS)
- Docker en ejecución (para PostgreSQL vía Dev Services y para empaquetar imágenes)

## Ejecución en desarrollo

En modo dev, Quarkus **Dev Services** levanta automáticamente un contenedor
PostgreSQL; no necesitas configurar la base de datos.

```bash
mvn quarkus:dev
```

- API:        http://localhost:8080/orders
- Swagger UI: http://localhost:8080/q/swagger-ui
- Health:     http://localhost:8080/q/health

## Endpoints

| Método | Ruta                    | Descripción            |
|--------|-------------------------|------------------------|
| POST   | `/orders`               | Crear orden            |
| GET    | `/orders`               | Listar órdenes         |
| GET    | `/orders/{id}`          | Obtener orden por id   |
| POST   | `/orders/{id}/confirm`  | Confirmar orden        |
| POST   | `/orders/{id}/cancel`   | Cancelar orden         |
| DELETE | `/orders/{id}`          | Eliminar orden         |

### Ejemplo: crear una orden

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
        "customerId": "cust-123",
        "items": [
          { "productId": "p-1", "productName": "Teclado", "quantity": 2, "unitPrice": 25.50 },
          { "productId": "p-2", "productName": "Mouse",   "quantity": 1, "unitPrice": 15.00 }
        ]
      }'
```

## Empaquetado y Docker

```bash
mvn package
docker build -f src/main/docker/Dockerfile.jvm -t order-service:jvm .
```

En producción la conexión a la base de datos se toma de variables de entorno:
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

## Esquema de base de datos

El esquema lo gestiona **Flyway** (`src/main/resources/db/migration`). Las
migraciones se aplican automáticamente al arrancar.

## Observabilidad (OpenTelemetry)

El servicio exporta **traces, métricas y logs** por **OTLP** mediante la
extensión `quarkus-opentelemetry`. El destino se configura con:

| Variable | Descripción | Default |
|----------|-------------|---------|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | Endpoint del OpenTelemetry Collector | `http://localhost:4317` |

En modo `dev` (`mvn quarkus:dev`) la exportación está **desactivada**
(`%dev.quarkus.otel.sdk.disabled=true`) para no requerir un collector local. El
`service.name` de la telemetría es `order-service` (de `quarkus.application.name`).

## Despliegue en Kubernetes

Los manifiestos y scripts viven en [`../../k8s`](../../k8s). El despliegue de
este servicio se automatiza con `k8s/scripts/deploy-order-service.sh` (build,
`kind load` y `kubectl apply`). Ver [`../../k8s/README.md`](../../k8s/README.md).
