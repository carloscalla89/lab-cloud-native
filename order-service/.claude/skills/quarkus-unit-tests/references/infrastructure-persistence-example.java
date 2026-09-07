// Ejemplo de referencia: test de un adaptador de persistencia de
// `infrastructure` con @QuarkusTest real (sin mocks de Hibernate/Panache).
// Basado en com.kcd.orderservice.infrastructure.persistence.OrderRepositoryAdapter.
// No es un archivo que se compile como parte de este skill; copiar y adaptar
// el patron al adaptador real que se este testeando.
//
// Requiere una base de datos disponible en tiempo de test. Si no hay
// %test.quarkus.datasource.* configurado explicitamente, Quarkus Dev
// Services levanta un contenedor Postgres automaticamente (necesita Docker
// activo). @TestTransaction hace rollback automatico al terminar cada test,
// asi los tests no ensucian datos entre si.
//
// NOTA DE VERIFICACION: el import de io.quarkus.test.TestTransaction viene
// transitivamente de la extension quarkus-narayana-jta (que ya trae
// quarkus-hibernate-orm-panache). Si al compilar el test no resuelve ese
// import en esta version del BOM de Quarkus, correr
// `mvn dependency:tree -Dincludes=io.quarkus:quarkus-narayana-jta` para
// confirmar que la extension esta en el classpath de test, y si el problema
// persiste, revisar la guia oficial https://quarkus.io/guides/getting-started-testing
// (seccion "Tests and Transactions") por si el paquete cambio de nombre.

package com.kcd.orderservice.infrastructure.persistence;

import com.kcd.orderservice.domain.model.Order;
import com.kcd.orderservice.domain.model.OrderItem;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class OrderRepositoryAdapterTest {

    @Inject
    OrderRepositoryAdapter repositoryAdapter;

    @Test
    @TestTransaction
    void save_persisteUnaOrdenNueva_yPuedeRecuperarseConFindById() {
        Order order = Order.create("cliente-1", List.of(
                new OrderItem("prod-1", "Producto 1", 2, BigDecimal.valueOf(10))));

        Order saved = repositoryAdapter.save(order);

        Optional<Order> found = repositoryAdapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo("cliente-1");
        assertThat(found.get().getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20));
    }

    @Test
    @TestTransaction
    void findById_devuelveVacio_cuandoNoExiste() {
        Optional<Order> found = repositoryAdapter.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    @TestTransaction
    void deleteById_eliminaLaOrden_yLuegoNoSeEncuentra() {
        Order order = Order.create("cliente-2", List.of(
                new OrderItem("prod-2", "Producto 2", 1, BigDecimal.valueOf(5))));
        Order saved = repositoryAdapter.save(order);

        boolean deleted = repositoryAdapter.deleteById(saved.getId());

        assertThat(deleted).isTrue();
        assertThat(repositoryAdapter.findById(saved.getId())).isEmpty();
    }

    @Test
    @TestTransaction
    void deleteById_devuelveFalse_cuandoNoExiste() {
        boolean deleted = repositoryAdapter.deleteById(UUID.randomUUID());

        assertThat(deleted).isFalse();
    }
}
