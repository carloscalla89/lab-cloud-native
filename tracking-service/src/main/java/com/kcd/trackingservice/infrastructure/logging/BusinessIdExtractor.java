package com.kcd.trackingservice.infrastructure.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriInfo;

import java.util.Optional;

/**
 * Extrae identificadores de negocio (orderId/customerId) de un request HTTP
 * de forma generica, sin conocer el DTO especifico de cada endpoint.
 *
 * <p>tracking-service no tiene concepto de customerId ni de orderId propio:
 * el envio se identifica con {@code shipmentId}, que por convencion del
 * sistema es el mismo {@code id} de la orden en order-service. El path param
 * {@code id} de {@code GET /tracking-events/{id}} identifica al *evento* de
 * tracking, no a la orden, y por eso NO se mapea a orderId.</p>
 */
@ApplicationScoped
public class BusinessIdExtractor {

    private static final String SHIPMENT_ID_QUERY_PARAM = "shipmentId";

    private final ObjectMapper objectMapper;

    public BusinessIdExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public record Ids(String orderId, String customerId) {
    }

    public Ids extract(UriInfo uriInfo, byte[] bodyBytes) {
        String orderId = fromShipmentQueryParam(uriInfo).orElse(fromBody(bodyBytes, "shipmentId"));
        return new Ids(orderId, null);
    }

    private Optional<String> fromShipmentQueryParam(UriInfo uriInfo) {
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
