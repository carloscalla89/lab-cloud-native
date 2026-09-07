# tracking-service

Microservicio en **Quarkus** (Java 21, Quarkus 3.33 LTS) que actúa como
**Anti-Corruption Layer (ACL)** del subdominio de **envíos**.

## Responsabilidad

1. Recibir coordenadas (latitud/longitud) asociadas a un envío.
2. Consultar la API externa de **Google Maps** para hacer **geocodificación
   inversa** (coordenadas → dirección).
3. Registrar y persistir el **evento de tracking** resultante.

## El patrón Anti-Corruption Layer

El modelo y los errores de los proveedores externos quedan **aislados** en la
capa de infraestructura y **nunca** penetran en el dominio:

- `domain/port/ReverseGeocodingPort` define la capacidad en lenguaje del
  dominio: *coordenadas → `Address`*. El dominio desconoce qué proveedor hay detrás.
- `infrastructure/geocoding/nominatim/*` y `infrastructure/geocoding/google/*`
  contienen los DTOs crudos y los clientes REST de cada proveedor. Solo viven aquí.
- Cada adaptador (`NominatimReverseGeocodingAdapter`,
  `GoogleMapsReverseGeocodingAdapter`) **traduce** la respuesta del proveedor al
  modelo limpio `Address` y convierte cualquier fallo técnico en una
  `GeocodingException` del dominio.

Como ambos implementan el mismo puerto, cambiar de proveedor (o agregar uno
nuevo como HERE o Mapbox) no toca el dominio ni la aplicación.

## Proveedores de geocodificación

Se selecciona con la propiedad `geocoding.provider` (evaluada en tiempo de
build con `@IfBuildProperty`):

| Proveedor   | Valor         | API key | Notas                                              |
|-------------|---------------|---------|----------------------------------------------------|
| **Nominatim** (OpenStreetMap) | `nominatim` *(por defecto)* | No | Gratuito. Política de uso: máx. ~1 req/s y `User-Agent` propio. Para producción intensiva, alojar una instancia propia. |
| **Google Maps** | `google`  | Sí      | Requiere `GOOGLE_MAPS_API_KEY` con la *Geocoding API* habilitada. |

Por defecto (sin configurar nada) se usa **Nominatim**, que no requiere clave.
Para usar Google: `GEOCODING_PROVIDER=google` y definir `GOOGLE_MAPS_API_KEY`.

## Arquitectura (Clean Architecture)

```
com.kcd.trackingservice
├── domain                      # Núcleo de negocio (sin frameworks)
│   ├── model                   # TrackingEvent, Coordinates, Address (value objects)
│   ├── exception               # InvalidCoordinates, Geocoding, TrackingEventNotFound
│   ├── port                    # ReverseGeocodingPort (frontera del ACL)
│   └── repository              # TrackingEventRepository (puerto de persistencia)
│
├── application                 # Casos de uso / orquestación
│   ├── dto
│   ├── mapper
│   └── service                 # TrackingApplicationService
│
└── infrastructure              # Detalles técnicos (adaptadores)
    ├── geocoding               # ACL de geocodificación (un adaptador por proveedor)
    │   ├── nominatim           # DTOs crudos + cliente REST de Nominatim (aislados)
    │   └── google              # DTOs crudos + cliente REST de Google (aislados)
    ├── persistence             # Entidad JPA, Panache, adaptador del puerto
    │   ├── entity
    │   └── mapper
    └── rest                    # Adaptador de entrada HTTP + mapeo de excepciones
        └── exception
```

## Requisitos

- JDK 21+
- Maven 3.9+
- Docker en ejecución
- (Opcional) Una **API key de Google Maps** solo si se usa ese proveedor

## Configuración

| Variable               | Descripción                                  | Default                       |
|------------------------|----------------------------------------------|-------------------------------|
| `GEOCODING_PROVIDER`   | Proveedor: `nominatim` o `google`            | `nominatim`                   |
| `NOMINATIM_URL`        | URL base de Nominatim                         | `https://nominatim.openstreetmap.org` |
| `GOOGLE_MAPS_API_KEY`  | Clave de API de Google (solo si `google`)    | *(vacía)*                     |
| `GOOGLE_MAPS_URL`      | URL base de Google                            | `https://maps.googleapis.com` |
| `DB_URL`               | URL JDBC de PostgreSQL                        | `jdbc:postgresql://localhost:5432/tracking` |
| `DB_USERNAME`          | Usuario de base de datos                      | `quarkusdbuser`               |
| `DB_PASSWORD`          | Clave de base de datos                        | *(ver application.properties)*|

## Ejecución en desarrollo

Con el proveedor por defecto (Nominatim) no necesitas ninguna clave:

```bash
cd tracking-service
mvn quarkus:dev
```

Para usar Google Maps en su lugar:

```bash
export GEOCODING_PROVIDER=google
export GOOGLE_MAPS_API_KEY="tu-api-key"
mvn quarkus:dev
```

- API:        http://localhost:8081/tracking-events
- Swagger UI: http://localhost:8081/q/swagger-ui
- Health:     http://localhost:8081/q/health

## Endpoints

| Método | Ruta                                   | Descripción                          |
|--------|----------------------------------------|--------------------------------------|
| POST   | `/tracking-events`                     | Registrar evento (geocodifica + persiste) |
| GET    | `/tracking-events/{id}`                | Obtener evento por id                |
| GET    | `/tracking-events?shipmentId=SHIP-1`   | Historial de tracking de un envío    |

### Ejemplo: registrar un evento

```bash
curl -X POST http://localhost:8081/tracking-events \
  -H "Content-Type: application/json" \
  -d '{
        "shipmentId": "SHIP-1",
        "latitude": -12.046374,
        "longitude": -77.042793
      }'
```

Respuesta `201 Created` con la dirección ya resuelta por geocodificación inversa.

## Códigos de error

| Situación                                   | HTTP |
|---------------------------------------------|------|
| Coordenadas fuera de rango / payload inválido | 400 |
| Evento no encontrado                        | 404  |
| Fallo o sin resultados de Google Maps       | 502  |

## Observabilidad (OpenTelemetry)

El servicio exporta **traces, métricas y logs** por **OTLP** mediante la
extensión `quarkus-opentelemetry`. El destino se configura con:

| Variable | Descripción | Default |
|----------|-------------|---------|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | Endpoint del OpenTelemetry Collector | `http://localhost:4317` |

En modo `dev` (`mvn quarkus:dev`) la exportación está **desactivada**
(`%dev.quarkus.otel.sdk.disabled=true`) para no requerir un collector local. El
`service.name` de la telemetría es `tracking-service` (de `quarkus.application.name`).

## Despliegue en Kubernetes

Los manifiestos y scripts viven en [`../../k8s`](../../k8s). El despliegue de
este servicio se automatiza con `k8s/scripts/deploy-tracking-service.sh` (build,
`kind load` y `kubectl apply`). Ver [`../../k8s/README.md`](../../k8s/README.md).
