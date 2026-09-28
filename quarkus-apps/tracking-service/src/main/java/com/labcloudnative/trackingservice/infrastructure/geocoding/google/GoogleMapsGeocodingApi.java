package com.labcloudnative.trackingservice.infrastructure.geocoding.google;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente REST tipado para la API de geocodificacion de Google Maps.
 *
 * <p>Es un detalle de infraestructura del Anti-Corruption Layer. La URL base
 * se configura con la clave {@code google-maps}
 * (ver {@code quarkus.rest-client.google-maps.url} en application.properties).</p>
 *
 * <p>Endpoint: {@code GET /maps/api/geocode/json?latlng=LAT,LNG&key=API_KEY}</p>
 */
@RegisterRestClient(configKey = "google-maps")
@Path("/maps/api/geocode")
public interface GoogleMapsGeocodingApi {

    /**
     * Invoca la geocodificacion inversa de Google Maps.
     *
     * @param latlng coordenadas en formato "lat,lng"
     * @param apiKey clave de API de Google
     * @return la respuesta cruda del proveedor
     */
    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    GoogleGeocodingResponse reverseGeocode(@QueryParam("latlng") String latlng,
                                           @QueryParam("key") String apiKey);
}
