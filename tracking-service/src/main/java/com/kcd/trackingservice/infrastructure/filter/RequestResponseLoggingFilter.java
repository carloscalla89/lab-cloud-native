package com.kcd.trackingservice.infrastructure.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcd.trackingservice.infrastructure.logging.BusinessIdExtractor;
import com.kcd.trackingservice.infrastructure.logging.LogTruncation;
import com.kcd.trackingservice.infrastructure.logging.MdcKeys;
import com.kcd.trackingservice.infrastructure.logging.TraceContext;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Loguea cada request/response entrante y puebla el MDC con el trace context
 * (traceId/spanId/parentId) y con identificadores de negocio (orderId/
 * customerId) para poder correlacionar logs entre servicios en Loki.
 */
@Provider
@ApplicationScoped
public class RequestResponseLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = LoggerFactory.getLogger("http.request");
    private static final String START_TIME_PROPERTY = "requestStartTimeNanos";
    private static final String HTTP_STATUS_CODE_ATTRIBUTE = "http.response.status_code";

    private final BusinessIdExtractor businessIdExtractor;
    private final ObjectMapper objectMapper;

    public RequestResponseLoggingFilter(BusinessIdExtractor businessIdExtractor, ObjectMapper objectMapper) {
        this.businessIdExtractor = businessIdExtractor;
        this.objectMapper = objectMapper;
    }

    // No @PreMatching: correria en el I/O thread de Vert.x (lectura bloqueante
    // prohibida) y antes del routing, sin path params todavia resueltos.
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        byte[] bodyBytes = bufferBody(requestContext);
        requestContext.setProperty(START_TIME_PROPERTY, System.nanoTime());

        MDC.put(MdcKeys.TRACE_ID, TraceContext.traceId());
        MDC.put(MdcKeys.SPAN_ID, TraceContext.spanId());
        MDC.put(MdcKeys.PARENT_ID, TraceContext.parentId());

        BusinessIdExtractor.Ids ids = businessIdExtractor.extract(requestContext.getUriInfo(), bodyBytes);
        if (ids.orderId() != null) {
            MDC.put(MdcKeys.ORDER_ID, ids.orderId());
        }
        if (ids.customerId() != null) {
            MDC.put(MdcKeys.CUSTOMER_ID, ids.customerId());
        }

        LOG.info("REQUEST {} {} body={}", requestContext.getMethod(), requestContext.getUriInfo().getPath(),
                LogTruncation.truncate(new String(bodyBytes, StandardCharsets.UTF_8)));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        try {
            int statusCode = responseContext.getStatus();

            // OpenTelemetry deja el status del span HTTP en UNSET por defecto. La
            // instrumentacion automatica de Quarkus solo lo marca ERROR cuando una excepcion
            // se propaga sin manejar; si un ExceptionMapper la captura y la traduce a una
            // Response (como con 502 de UpstreamServiceException), el span nunca se entera y
            // se queda UNSET. Se fija el status explicitamente segun el status code HTTP real
            // (5xx = ERROR, resto = OK) para que Tempo/Grafana (Service Graph, tasas de error)
            // reflejen el mismo resultado que ya se ve en los logs de Loki.
            Span.current().setStatus(statusCode >= 500 ? StatusCode.ERROR : StatusCode.OK);
            Span.current().setAttribute(HTTP_STATUS_CODE_ATTRIBUTE, statusCode);

            MDC.put(MdcKeys.STATUS_CODE, String.valueOf(statusCode));

            long durationMs = durationInMs(requestContext);
            String body = serialize(responseContext.getEntity());
            String message = "RESPONSE {} {} status={} durationMs={} body={}";
            Object[] args = {requestContext.getMethod(), requestContext.getUriInfo().getPath(),
                    statusCode, durationMs, LogTruncation.truncate(body)};

            // 4xx (errores controlados, mapeados por los ExceptionMapper de dominio) y 5xx
            // (errores no controlados) se loguean en ERROR para poder filtrarlos en Loki;
            // el flujo correcto (2xx/3xx) se mantiene en INFO.
            if (statusCode >= 400) {
                LOG.error(message, args);
            } else {
                LOG.info(message, args);
            }
        } finally {
            MDC.clear();
        }
    }

    private long durationInMs(ContainerRequestContext requestContext) {
        Object startTime = requestContext.getProperty(START_TIME_PROPERTY);
        if (!(startTime instanceof Long startNanos)) {
            return -1;
        }
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private byte[] bufferBody(ContainerRequestContext requestContext) throws IOException {
        InputStream entityStream = requestContext.getEntityStream();
        if (entityStream == null) {
            return new byte[0];
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        entityStream.transferTo(buffer);
        byte[] bytes = buffer.toByteArray();
        requestContext.setEntityStream(new ByteArrayInputStream(bytes));
        return bytes;
    }

    private String serialize(Object entity) {
        if (entity == null) {
            return "";
        }
        if (entity instanceof String string) {
            return string;
        }
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (Exception e) {
            return String.valueOf(entity);
        }
    }
}
