package com.labcloudnative.trackingservice.infrastructure.geocoding;

import com.labcloudnative.trackingservice.domain.model.Address;
import com.labcloudnative.trackingservice.domain.model.Coordinates;
import com.labcloudnative.trackingservice.domain.port.ReverseGeocodingPort;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Decorator del ACL de geocodificacion que agrega latencia artificial
 * configurable, deshabilitada por defecto. Pensado para demos de
 * troubleshooting con tracing: activarla en vivo (sin rebuild) via env var
 * simula que el proveedor externo se puso lento, y el span propio queda
 * visible en la traza junto al span real de la llamada HTTP, dejando en
 * evidencia el tiempo no explicado por la red.
 *
 * <p>Envuelve cualquier adaptador activo de {@link ReverseGeocodingPort}
 * (Nominatim o Google), sea cual sea el proveedor elegido en build time.</p>
 */
@Decorator
@Priority(2000)
public class FaultInjectionGeocodingDecorator implements ReverseGeocodingPort {

    private static final Logger LOG = Logger.getLogger(FaultInjectionGeocodingDecorator.class);

    @Inject
    @Delegate
    ReverseGeocodingPort delegate;

    @Inject
    Tracer tracer;

    @ConfigProperty(name = "fault.latency.enabled", defaultValue = "false")
    boolean enabled;

    @ConfigProperty(name = "fault.latency.delay-ms", defaultValue = "0")
    long delayMs;

    @ConfigProperty(name = "fault.latency.jitter-ms", defaultValue = "0")
    long jitterMs;

    @Override
    public Address reverseGeocode(Coordinates coordinates) {
        if (!enabled || delayMs <= 0) {
            return delegate.reverseGeocode(coordinates);
        }

        // Span propio y explicito (no via @WithSpan: no se propaga de forma
        // fiable a traves del decorator de CDI) para que en la traza quede
        // claramente visible cuanto tiempo se "pierde" antes de la llamada
        // real al proveedor de geocodificacion.
        Span span = tracer.spanBuilder("fault-injection.geocoding-delay").startSpan();
        try (Scope scope = span.makeCurrent()) {
            long totalDelayMs = injectLatency(span);
            span.setAttribute("fault.injection.delay_ms", totalDelayMs);
            return delegate.reverseGeocode(coordinates);
        } finally {
            span.end();
        }
    }

    /**
     * Duerme el hilo actual para simular latencia del proveedor externo.
     *
     * @return el delay total aplicado, en milisegundos
     */
    private long injectLatency(Span span) {
        long jitter = jitterMs > 0 ? ThreadLocalRandom.current().nextLong(jitterMs + 1) : 0;
        long totalDelayMs = delayMs + jitter;

        LOG.warnf("Fault injection activa: agregando %d ms de latencia artificial antes de geocodificar", totalDelayMs);
        span.addEvent("fault-injection.sleep.start");
        try {
            Thread.sleep(totalDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        span.addEvent("fault-injection.sleep.end");
        return totalDelayMs;
    }
}
