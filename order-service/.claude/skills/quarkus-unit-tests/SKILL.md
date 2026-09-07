---
name: quarkus-unit-tests
description: Genera y mantiene tests unitarios/de integracion para las aplicaciones de Quarkus (Java 21 + Quarkus, arquitectura hexagonal). Usar SIEMPRE que se pida crear, revisar o completar tests para clases de los proyectos solicitados. Aplica MockitoExtension a clases de la capa application y QuarkusTest a clases de la capa infrastructure. Trigger words: "test", "unit test", "prueba unitaria", "testear", "cobertura", "JUnit", "Mockito", "QuarkusTest".
---

# Tests unitarios para proyectos (Quarkus + arquitectura hexagonal)

## Regla de enrutamiento (por paquete de la clase a testear)

Esta regla aplica a cualquier proyecto Quarkus con arquitectura hexagonal, sin importar su groupId/paquete base (`com.kcd.orderservice`, `com.acme.paymentservice`, etc.). Antes de aplicar la tabla, confirma la convencion de paquetes del proyecto actual mirando `src/main/java/**` (normalmente `<paquete-base>.application`, `<paquete-base>.infrastructure`, `<paquete-base>.domain`; si el proyecto usa otros nombres como `usecase`, `adapter.in`/`adapter.out`, `core`, trata esos paquetes como equivalentes de application/infrastructure/domain segun su rol).

| Paquete de la clase                              | Framework de test             | Por que |
|---------------------------------------------------|--------------------------------|---------|
| `<paquete-base>.application.*`                    | JUnit 5 + `MockitoExtension`   | Son casos de uso puros que orquestan puertos (repositorios, mappers). No necesitan contenedor CDI: se mockean las dependencias. |
| `<paquete-base>.infrastructure.*`                 | `@QuarkusTest`                  | Son adaptadores (REST, persistencia, clientes, filtros) que dependen de infraestructura real de Quarkus (CDI, JAX-RS, Hibernate/Panache). |
| `<paquete-base>.domain.*` (si se pide)            | JUnit 5 plano, sin extension    | Son objetos de dominio sin dependencias externas (entidades, value objects, excepciones). No necesitan mocks ni contenedor. |

Antes de escribir un test, mira el paquete de la clase objetivo y aplica la fila correspondiente. No mezclar: una clase de `application` NUNCA lleva `@QuarkusTest`, y un adaptador de `infrastructure` NUNCA se testea solo con mocks si su valor es justamente la integracion con la tecnologia (JAX-RS, Hibernate, etc.) — en ese caso usa `@QuarkusTest`.

Los ejemplos en `references/` (`OrderApplicationService`, `OrderResource`, `OrderRepositoryAdapter`) son una ilustracion concreta tomada de order-service; el patron (anotaciones, estructura del test) se copia y adapta a la clase real del proyecto en el que se este trabajando, sin importar que su paquete no sea `com.kcd.orderservice`.

## Dependencia de Maven requerida

Antes de escribir el primer test, verifica en el `pom.xml` del proyecto si ya existen `quarkus-junit` (o su nombre anterior `quarkus-junit5`), `quarkus-junit-mockito` (o `quarkus-junit5-mockito`) y `rest-assured` con `scope=test`; si falta alguna, agregala. Esta unica dependencia de mockito alcanza para ambas capas: trae transitivamente `mockito-junit-jupiter` (para `MockitoExtension` en `application`) y el soporte de `@InjectMock` (para `@QuarkusTest` en `infrastructure`), sin fijar versiones manuales — las gestiona el BOM de Quarkus del proyecto.

Si el proyecto tiene `quarkus-smallrye-jwt` (autenticacion JWT), valida ademas si ya existe `quarkus-test-security`; si no, agregala tambien — es necesaria para simular usuarios/roles autenticados en tests `@QuarkusTest` de endpoints protegidos.


## Ubicacion y nombres de archivo

Espeja el paquete de la clase de produccion bajo `src/test/java`, con el sufijo `Test` (mismo paquete base que tenga el proyecto):

```
src/main/java/<paquete-base>/application/service/XxxService.java
  -> src/test/java/<paquete-base>/application/service/XxxServiceTest.java

src/main/java/<paquete-base>/infrastructure/rest/XxxResource.java
  -> src/test/java/<paquete-base>/infrastructure/rest/XxxResourceTest.java
```

Ejemplo real (order-service): `src/main/java/com/kcd/orderservice/application/service/OrderApplicationService.java` -> `src/test/java/com/kcd/orderservice/application/service/OrderApplicationServiceTest.java`.

## Capa `application`: patron MockitoExtension

Ver ejemplo completo en [references/application-layer-example.java](references/application-layer-example.java) (basado en `OrderApplicationService`).

Puntos clave:
- `@ExtendWith(MockitoExtension.class)` a nivel de clase.
- `@Mock` para cada puerto/colaborador (`OrderRepository`, `OrderDtoMapper`), `@InjectMocks` para la clase bajo prueba.
- Sin `@ApplicationScoped`, sin contenedor CDI: instanciacion pura vía Mockito.
- Estructura Arrange/Act/Assert (o Given/When/Then), un metodo de test por caso de uso/rama (camino feliz + excepciones de dominio como `OrderNotFoundException`).
- Verificar interacciones relevantes con `verify(mock).metodo(...)` cuando el comportamiento importa (por ejemplo, que se llama `save` tras `confirm()`), no solo el valor de retorno.
- No uses `@QuarkusTest` aqui: no hace falta arrancar Quarkus para testear orquestacion pura.

## Capa `infrastructure`: patron QuarkusTest

Dentro de `infrastructure` hay dos sabores distintos; identifica cual aplica antes de escribir el test:

### a) Adaptadores REST (`infrastructure.rest.*`, ej. `OrderResource`)

Ver [references/infrastructure-rest-example.java](references/infrastructure-rest-example.java).

- `@QuarkusTest` a nivel de clase.
- `@InjectMock` sobre el servicio de aplicacion del que depende el resource (ej. `OrderApplicationService`) para aislar la capa HTTP de la logica de negocio — el objetivo de este test es el mapeo HTTP (status codes, path params, body), no repetir los casos de uso.
- Llamadas HTTP con RestAssured (`given().when().post("/orders")...`), asserts sobre `statusCode()` y el cuerpo JSON.
- Configura el mock con `Mockito.when(...)` por test; usa `Mockito.verify` si importa que se haya invocado con los argumentos correctos.

### b) Adaptadores de persistencia (`infrastructure.persistence.*`, ej. `OrderRepositoryAdapter`, `OrderPanacheRepository`)

Ver [references/infrastructure-persistence-example.java](references/infrastructure-persistence-example.java).

- `@QuarkusTest` a nivel de clase, inyectando el adaptador real (constructor injection vía CDI, no mocks) para probar la integracion real con Hibernate/Panache.
- `@TestTransaction` en cada metodo de test (JUnit) para que los cambios se hagan rollback automaticamente al terminar el test y no ensucien la base de datos entre tests.
- Requiere una base de datos disponible en tiempo de test: Quarkus Dev Services levanta un contenedor Postgres automaticamente si Docker esta corriendo y no hay `%test.quarkus.datasource.*` configurado explicitamente. Si el test falla por falta de datasource, revisa que Docker Desktop este activo antes de asumir un bug de codigo.
- No mockees `OrderPanacheRepository` ni Hibernate aqui: si se mockea la persistencia, se pierde el unico valor de este test (verificar el mapeo real a filas/columnas).

### c) Otros adaptadores (filters, clients, logging)

Mismo criterio que (a): `@QuarkusTest` + `@InjectMock` de sus colaboradores externos si los hay (ej. mockear el cliente REST saliente al testear `TrackingConnectivityResource`), enfocando el test en el comportamiento propio del adaptador (headers, MDC, manejo de excepciones), no en lo que ya cubre el test de application.

## Checklist antes de dar el test por terminado

1. ¿El paquete de la clase determino correctamente el framework (tabla de arriba)?
2. ¿El nombre y la ubicacion del archivo de test espejan el paquete de produccion?
3. ¿Se cubrio el camino feliz y al menos una rama de error/excepcion de dominio quan aplica (`OrderNotFoundException`, `InvalidOrderStateException`)?
4. Para `application`: ¿ninguna anotacion de Quarkus/CDI quedo en el test?
5. Para `infrastructure`: ¿el test realmente ejercita la integracion (HTTP real via RestAssured, o persistencia real via Panache) en vez de mockear justo lo que se supone que se prueba?
6. Ejecutar `mvn test -Dtest=<NombreDeLaClase>Test` (el proyecto no tiene `mvnw`, usa el Maven instalado) para confirmar que el test pasa antes de reportarlo como listo.
7. Si se uso `@TestTransaction` en un test de persistencia y el import `io.quarkus.test.TestTransaction` no resuelve al compilar, no lo reemplaces a ciegas: correr `mvn dependency:tree -Dincludes=io.quarkus:quarkus-narayana-jta` para confirmar que la extension esta presente, y si sigue sin resolver, consultar la guia oficial (https://quarkus.io/guides/getting-started-testing, seccion "Tests and Transactions") por si el paquete cambio en esta version del BOM.
