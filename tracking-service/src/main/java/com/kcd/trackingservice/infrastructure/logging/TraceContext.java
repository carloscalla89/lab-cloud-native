package com.kcd.trackingservice.infrastructure.logging;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.trace.ReadableSpan;

/**
 * Lee el trace context activo (OpenTelemetry) para poblar el MDC.
 */
public final class TraceContext {

    private static final String NOT_AVAILABLE = "n/a";

    private TraceContext() {
    }

    public static String traceId() {
        SpanContext spanContext = Span.current().getSpanContext();
        return spanContext.isValid() ? spanContext.getTraceId() : NOT_AVAILABLE;
    }

    public static String spanId() {
        SpanContext spanContext = Span.current().getSpanContext();
        return spanContext.isValid() ? spanContext.getSpanId() : NOT_AVAILABLE;
    }

    public static String parentId() {
        Span current = Span.current();
        if (current instanceof ReadableSpan readableSpan) {
            SpanContext parentContext = readableSpan.getParentSpanContext();
            if (parentContext.isValid()) {
                return parentContext.getSpanId();
            }
        }
        return NOT_AVAILABLE;
    }
}
