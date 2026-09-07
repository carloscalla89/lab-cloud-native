package com.kcd.orderservice.infrastructure.persistence;

import com.kcd.orderservice.domain.model.Order;
import com.kcd.orderservice.domain.repository.OrderRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Decorator del puerto de persistencia que agrega latencia artificial
 * configurable antes de cada operacion con la base de datos.
 *
 * <p>Permite simular en vivo (sin rebuild) que PostgreSQL se ha puesto lento:
 * un problema habitual en produccion que puede ser dificil de aislar sin
 * trazabilidad distribuida. Al activarlo, el span
 * {@code fault-injection.db-delay} aparece en Tempo justo antes del span
 * de la consulta SQL, dejando en evidencia donde se pierde el tiempo.</p>
 *
 * <p>Se activa/desactiva cambiando la variable de entorno
 * {@code FAULT_DB_LATENCY_ENABLED} en el Deployment de order-service
 * (sin rebuild, solo un rollout restart).</p>
 */
@Decorator
@Priority(2000)
public class FaultInjectionOrderRepositoryDecorator implements OrderRepository {

    private static final Logger LOG = Logger.getLogger(FaultInjectionOrderRepositoryDecorator.class);

    @Inject
    @Delegate
    OrderRepository delegate;

    @Inject
    Tracer tracer;

    @ConfigProperty(name = "fault.db.latency.enabled", defaultValue = "false")
    boolean enabled;

    @ConfigProperty(name = "fault.db.latency.delay-ms", defaultValue = "0")
    long delayMs;

    @ConfigProperty(name = "fault.db.latency.jitter-ms", defaultValue = "0")
    long jitterMs;

    @Override
    public Order save(Order order) {
        injectLatencyIfEnabled("save");
        return delegate.save(order);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        injectLatencyIfEnabled("findById");
        return delegate.findById(id);
    }

    @Override
    public List<Order> findAll() {
        injectLatencyIfEnabled("findAll");
        return delegate.findAll();
    }

    @Override
    public boolean deleteById(UUID id) {
        injectLatencyIfEnabled("deleteById");
        return delegate.deleteById(id);
    }

    /**
     * Inyecta la latencia artificial antes de la operacion indicada, creando
     * un span propio en la traza activa para que sea visible en Tempo.
     *
     * @param operation nombre de la operacion del repositorio (para atributos del span)
     */
    private void injectLatencyIfEnabled(String operation) {
        if (!enabled || delayMs <= 0) {
            return;
        }

        Span span = tracer.spanBuilder("fault-injection.db-delay").startSpan();
        try (Scope scope = span.makeCurrent()) {
            long jitter = jitterMs > 0 ? ThreadLocalRandom.current().nextLong(jitterMs + 1) : 0;
            long totalDelay = delayMs + jitter;

            span.setAttribute("fault.injection.db.operation", operation);
            span.setAttribute("fault.injection.delay_ms", totalDelay);
            span.addEvent("fault-injection.sleep.start");

            LOG.warnf("Fault injection activa: agregando %d ms de latencia artificial en DB.%s", totalDelay, operation);
            try {
                Thread.sleep(totalDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            span.addEvent("fault-injection.sleep.end");
        } finally {
            span.end();
        }
    }
}
