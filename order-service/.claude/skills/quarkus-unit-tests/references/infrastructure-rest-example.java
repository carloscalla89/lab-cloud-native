// Ejemplo de referencia: test de un adaptador REST de `infrastructure` con
// @QuarkusTest + @InjectMock + RestAssured.
// Basado en com.kcd.orderservice.infrastructure.rest.OrderResource.
// No es un archivo que se compile como parte de este skill; copiar y adaptar
// el patron al resource real que se este testeando.
//
// Objetivo del test: el mapeo HTTP (status codes, path params, body JSON),
// NO repetir los casos de uso de OrderApplicationService (eso ya lo cubre
// OrderApplicationServiceTest con MockitoExtension). Por eso el servicio de
// aplicacion se mockea con @InjectMock. Los DTOs de application.dto son
// records: se construyen instancias reales en vez de mockearlos.

package com.kcd.orderservice.infrastructure.rest;

import com.kcd.orderservice.application.dto.OrderResponse;
import com.kcd.orderservice.application.service.OrderApplicationService;
import com.kcd.orderservice.domain.exception.OrderNotFoundException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class OrderResourceTest {

    @InjectMock
    OrderApplicationService orderService;

    @Test
    void get_devuelve200ConLaOrden_cuandoExiste() {
        UUID id = UUID.randomUUID();
        OrderResponse response = new OrderResponse(
                id, "cliente-1", "PENDING", BigDecimal.TEN, List.of(), Instant.now(), Instant.now());

        when(orderService.getOrder(id)).thenReturn(response);

        given()
                .when().get("/orders/{id}", id)
                .then()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.equalTo(id.toString()));
    }

    @Test
    void get_devuelve404_cuandoNoExiste() {
        UUID id = UUID.randomUUID();
        when(orderService.getOrder(id)).thenThrow(new OrderNotFoundException(id));

        given()
                .when().get("/orders/{id}", id)
                .then()
                .statusCode(404);
    }

    @Test
    void create_devuelve201ConLocationHeader() {
        OrderResponse response = new OrderResponse(
                UUID.randomUUID(), "cliente-1", "PENDING", BigDecimal.ZERO, List.of(), Instant.now(), Instant.now());
        when(orderService.createOrder(any())).thenReturn(response);

        given()
                .contentType("application/json")
                .body("""
                        {"customerId": "cliente-1", "items": []}
                        """)
                .when().post("/orders")
                .then()
                .statusCode(201)
                .header("Location", containsString("/orders/"));
    }

    @Test
    void list_devuelveLaListaDelServicio() {
        when(orderService.listOrders()).thenReturn(List.of());

        given()
                .when().get("/orders")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    void delete_devuelve204_cuandoSeElimina() {
        UUID id = UUID.randomUUID();

        given()
                .when().delete("/orders/{id}", id)
                .then()
                .statusCode(204);
    }
}
