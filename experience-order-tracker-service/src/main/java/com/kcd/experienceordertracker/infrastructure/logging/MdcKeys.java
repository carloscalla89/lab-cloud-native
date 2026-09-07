package com.kcd.experienceordertracker.infrastructure.logging;

/**
 * Nombres de las claves usadas en el MDC para correlacionar logs con trazas
 * (OpenTelemetry) y con identificadores de negocio.
 */
public final class MdcKeys {

    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String PARENT_ID = "parentId";
    public static final String ORDER_ID = "orderId";
    public static final String CUSTOMER_ID = "customerId";
    public static final String STATUS_CODE = "statusCode";

    private MdcKeys() {
    }
}
