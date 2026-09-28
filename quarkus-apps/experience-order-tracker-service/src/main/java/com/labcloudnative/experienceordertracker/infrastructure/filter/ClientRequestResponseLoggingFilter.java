package com.labcloudnative.experienceordertracker.infrastructure.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labcloudnative.experienceordertracker.infrastructure.logging.LogTruncation;
import com.labcloudnative.experienceordertracker.infrastructure.logging.MdcKeys;
import io.opentelemetry.api.trace.Span;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
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
 * Loguea cada llamada saliente hecha con un REST Client (MicroProfile Rest
 * Client) hacia order-service/tracking-service. El MDC (traceId/spanId/
 * parentId/orderId/customerId) ya fue poblado por
 * {@code RequestResponseLoggingFilter} para el request entrante que origina
 * esta llamada, en el mismo hilo (llamadas sincronas/bloqueantes).
 */
@Provider
@ApplicationScoped
public class ClientRequestResponseLoggingFilter implements ClientRequestFilter, ClientResponseFilter {

    private static final Logger LOG = LoggerFactory.getLogger("http.client");
    private static final String START_TIME_PROPERTY = "clientRequestStartTimeNanos";
    private static final String HTTP_STATUS_CODE_ATTRIBUTE = "http.response.status_code";

    private final ObjectMapper objectMapper;

    public ClientRequestResponseLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void filter(ClientRequestContext requestContext) {
        requestContext.setProperty(START_TIME_PROPERTY, System.nanoTime());
        LOG.info("CLIENT REQUEST {} {} body={}", requestContext.getMethod(), requestContext.getUri(),
                LogTruncation.truncate(serialize(requestContext.getEntity())));
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        int statusCode = responseContext.getStatus();
        Span.current().setAttribute(HTTP_STATUS_CODE_ATTRIBUTE, statusCode);
        MDC.put(MdcKeys.STATUS_CODE, String.valueOf(statusCode));

        long durationMs = durationInMs(requestContext);
        byte[] bodyBytes = bufferBody(responseContext);
        String message = "CLIENT RESPONSE {} {} status={} durationMs={} body={}";
        Object[] args = {requestContext.getMethod(), requestContext.getUri(), statusCode, durationMs,
                LogTruncation.truncate(new String(bodyBytes, StandardCharsets.UTF_8))};

        // 4xx (errores controlados, mapeados por los ExceptionMapper de dominio) y 5xx
        // (errores no controlados) se loguean en ERROR para poder filtrarlos en Loki;
        // el flujo correcto (2xx/3xx) se mantiene en INFO.
        if (statusCode >= 400) {
            LOG.error(message, args);
        } else {
            LOG.info(message, args);
        }
    }

    private long durationInMs(ClientRequestContext requestContext) {
        Object startTime = requestContext.getProperty(START_TIME_PROPERTY);
        if (!(startTime instanceof Long startNanos)) {
            return -1;
        }
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private byte[] bufferBody(ClientResponseContext responseContext) throws IOException {
        InputStream entityStream = responseContext.getEntityStream();
        if (entityStream == null) {
            return new byte[0];
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        entityStream.transferTo(buffer);
        byte[] bytes = buffer.toByteArray();
        responseContext.setEntityStream(new ByteArrayInputStream(bytes));
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
