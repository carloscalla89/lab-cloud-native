package com.labcloudnative.orderservice.infrastructure.client;

import com.labcloudnative.orderservice.infrastructure.filter.ClientRequestResponseLoggingFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente REST hacia tracking-service usado exclusivamente para demostrar
 * la denegación por Istio AuthorizationPolicy (tracking-allow-experience)
 *
 * <p>Llama a {@code GET /tracking-events?shipmentId=...} con un timeout
 * corto (2 s) para que el fallo sea rapido: el sidecar de tracking-service
 * rechaza la peticion con 403 porque solo el BFF esta autorizado.</p>
 */
@RegisterRestClient(configKey = "tracking-service")
@RegisterProvider(ClientRequestResponseLoggingFilter.class)
@Path("/tracking-events")
public interface TrackingServiceDemoClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    String getEventsByShipment(@QueryParam("shipmentId") String shipmentId);
}
