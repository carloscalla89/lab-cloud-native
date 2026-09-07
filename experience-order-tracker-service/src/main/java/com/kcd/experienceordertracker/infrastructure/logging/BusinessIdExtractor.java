package com.kcd.experienceordertracker.infrastructure.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;
import java.util.Optional;

/**
 * Extrae identificadores de negocio (orderId/customerId) de un request HTTP
 * de forma generica, sin conocer el DTO especifico de cada endpoint.
 *
 * <p>El BFF usa {@code orderId} como path param ({@code GET /experience/orders/{orderId}})
 * y {@code shipmentId} como query param ({@code GET /experience/tracking-events?shipmentId=...}),
 * que por convencion del sistema es el mismo id de la orden.</p>
 */
@ApplicationScoped
public class BusinessIdExtractor {

    private static final List<String> ORDER_ID_PATH_PARAMS = List.of("orderId");
    private static final String SHIPMENT_ID_QUERY_PARAM = "shipmentId";

    private final ObjectMapper objectMapper;

    public BusinessIdExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public record Ids(String orderId, String customerId) {
    }

    public Ids extract(UriInfo uriInfo, byte[] bodyBytes) {
        String orderId = fromParams(uriInfo).orElse(fromBody(bodyBytes, "orderId", "shipmentId"));
        String customerId = fromBody(bodyBytes, "customerId");
        return new Ids(orderId, customerId);
    }

    private Optional<String> fromParams(UriInfo uriInfo) {
        for (String param : ORDER_ID_PATH_PARAMS) {
            String value = uriInfo.getPathParameters().getFirst(param);
            if (value != null) {
                return Optional.of(value);
            }
        }
        return Optional.ofNullable(uriInfo.getQueryParameters().getFirst(SHIPMENT_ID_QUERY_PARAM));
    }

    private String fromBody(byte[] bodyBytes, String... fieldNames) {
        if (bodyBytes == null || bodyBytes.length == 0) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(bodyBytes);
            for (String field : fieldNames) {
                JsonNode node = root.get(field);
                if (node != null && node.isTextual()) {
                    return node.asText();
                }
            }
        } catch (Exception e) {
            // body no es JSON valido (o no es un objeto) - se ignora, no es critico para el logging
        }
        return null;
    }
}
