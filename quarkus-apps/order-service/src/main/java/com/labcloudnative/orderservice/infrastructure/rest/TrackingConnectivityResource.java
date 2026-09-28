package com.labcloudnative.orderservice.infrastructure.rest;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import com.labcloudnative.orderservice.infrastructure.client.TrackingServiceDemoClient;
import io.quarkus.logging.Log;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Endpoint de demo: demuestra que la Istio AuthorizationPolicy
 * {@code tracking-allow-experience} deniega la llamada de order-service hacia
 * tracking-service.
 *
 * <p>Llama directamente a tracking-service (algo que nunca deberia ocurrir en
 * produccion: el flujo real pasa por el BFF). La peticion es denegada por el
 * sidecar de tracking-service, que solo autoriza al ServiceAccount del BFF,
 * con la identidad de ambos extremos y el puerto 8081.</p>
 *
 * <p>Uso:
 * <pre>
 *   GET /orders/{orderId}/tracking-status
 * </pre>
 * Sustituye {@code {orderId}} por cualquier UUID (no necesita ser una orden
 * existente: el call se corta antes de llegar a tracking-service).
 * </p>
 */
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
public class TrackingConnectivityResource {

    @RestClient
    TrackingServiceDemoClient trackingClient;

    @ConfigProperty(name = "quarkus.rest-client.tracking-service.url")
    String trackingServiceUrl;

    /**
     * Intenta llamar a tracking-service para obtener los eventos del envio
     * asociado a la orden indicada.
     *
     * <p>Con la AuthorizationPolicy activa este endpoint siempre devuelve
     * {@code 503} porque el sidecar de tracking-service rechaza la peticion
     * (403) antes de que llegue a la aplicacion. Sin la policy, devuelve
     * {@code 200} con los eventos reales (o una lista vacia si no hay tracking
     * para ese shipment).</p>
     *
     * @param orderId identificador de la orden (se usa como shipmentId)
     * @return resultado de la prueba de conectividad con informacion de debug
     */
    @GET
    @Path("/{orderId}/tracking-status")
    public Response checkTrackingConnectivity(@PathParam("orderId") UUID orderId) {
        String shipmentId = orderId.toString();
        Log.warnf("[DEMO ISTIO] order-service intentando llamar a tracking-service " +
                  "para el envio %s — deberia ser bloqueado por la AuthorizationPolicy", shipmentId);
        try {
            String payload = trackingClient.getEventsByShipment(shipmentId);
            Log.warnf("[DEMO ISTIO] INESPERADO: la llamada tuvo exito. " +
                      "Verificar que la AuthorizationPolicy tracking-allow-experience este aplicada.");
            return Response.ok(new ConnectivityResult(
                    orderId,
                    trackingServiceUrl + "/tracking-events?shipmentId=" + shipmentId,
                    true,
                    payload,
                    null,
                    Instant.now()
            )).build();
        } catch (Exception e) {
            String motivo = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            Log.infof("[DEMO ISTIO] Llamada bloqueada como se esperaba: %s", motivo);
            return Response.status(503).entity(new ConnectivityResult(
                    orderId,
                    trackingServiceUrl + "/tracking-events?shipmentId=" + shipmentId,
                    false,
                    null,
                    "Bloqueado por Istio AuthorizationPolicy 'tracking-allow-experience' " +
                    "(egress de order-service hacia tracking-service denegado). " +
                    "Detalle: " + motivo,
                    Instant.now()
            )).build();
        }
    }

    /**
     * Resultado de la prueba de conectividad hacia tracking-service.
     *
     * @param orderId       identificador de la orden probada
     * @param targetUrl     URL a la que se intentó llamar
     * @param reachable     {@code true} si tracking-service respondio (policy ausente)
     * @param payload       respuesta de tracking-service si {@code reachable} es true
     * @param error         descripcion del bloqueo si {@code reachable} es false
     * @param checkedAt     momento de la comprobacion
     */
    public record ConnectivityResult(
            UUID orderId,
            String targetUrl,
            boolean reachable,
            String payload,
            String error,
            Instant checkedAt
    ) {}
}
