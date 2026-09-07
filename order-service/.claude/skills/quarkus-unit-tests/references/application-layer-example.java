// Ejemplo de referencia: test de capa `application` con MockitoExtension.
// Basado en com.kcd.orderservice.application.service.OrderApplicationService.
// No es un archivo que se compile como parte de este skill; copiar y adaptar
// el patron a la clase real que se este testeando.
//
// AssertJ (org.assertj.core.api.Assertions) esta disponible transitivamente
// via io.quarkus:quarkus-junit5 (scope test) sin necesidad de agregarlo aparte.

package com.kcd.orderservice.application.service;

import com.kcd.orderservice.application.dto.OrderResponse;
import com.kcd.orderservice.application.mapper.OrderDtoMapper;
import com.kcd.orderservice.domain.exception.OrderNotFoundException;
import com.kcd.orderservice.domain.model.Order;
import com.kcd.orderservice.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderDtoMapper mapper;

    @InjectMocks
    OrderApplicationService service;

    @Test
    void getOrder_devuelveLaOrdenMapeada_cuandoExiste() {
        UUID id = UUID.randomUUID();
        Order order = mock(Order.class);
        OrderResponse expectedResponse = mock(OrderResponse.class);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(mapper.toResponse(order)).thenReturn(expectedResponse);

        OrderResponse result = service.getOrder(id);

        assertThat(result).isSameAs(expectedResponse);
    }

    @Test
    void getOrder_lanzaOrderNotFoundException_cuandoNoExiste() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrder(id))
                .isInstanceOf(OrderNotFoundException.class);

        verify(mapper, never()).toResponse(any());
    }

    @Test
    void confirmOrder_confirmaYPersisteLaOrden() {
        UUID id = UUID.randomUUID();
        Order order = mock(Order.class);
        Order saved = mock(Order.class);
        OrderResponse expectedResponse = mock(OrderResponse.class);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(expectedResponse);

        OrderResponse result = service.confirmOrder(id);

        verify(order).confirm();
        verify(orderRepository).save(order);
        assertThat(result).isSameAs(expectedResponse);
    }

    @Test
    void deleteOrder_lanzaOrderNotFoundException_cuandoNoSeElimino() {
        UUID id = UUID.randomUUID();
        when(orderRepository.deleteById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteOrder(id))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
