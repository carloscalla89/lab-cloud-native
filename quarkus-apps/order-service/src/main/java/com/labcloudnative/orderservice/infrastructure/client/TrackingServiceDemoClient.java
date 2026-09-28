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
 * el bloqueo de egress por CiliumNetworkPolicy
 *
 * <p>Llama a {@code GET /tracking-events?shipmentId=...} con un timeout
 * corto (2 s) para que el fallo sea rapido y visible en Hubble como
 * {@code EGRESS DENIED} desde order-service hacia tracking-service.</p>
 */
@RegisterRestClient(configKey = "tracking-service")
@RegisterProvider(ClientRequestResponseLoggingFilter.class)
@Path("/tracking-events")
public interface TrackingServiceDemoClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    String getEventsByShipment(@QueryParam("shipmentId") String shipmentId);
}
