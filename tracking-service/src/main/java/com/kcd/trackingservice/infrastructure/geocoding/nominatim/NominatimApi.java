package com.kcd.trackingservice.infrastructure.geocoding.nominatim;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente REST tipado para la API de geocodificacion inversa de Nominatim
 * (OpenStreetMap). Es gratuita y no requiere clave de API.
 *
 * <p>La URL base se configura con la clave {@code nominatim}
 * (ver {@code quarkus.rest-client.nominatim.url} en application.properties).</p>
 *
 * <p>La politica de uso de Nominatim exige enviar un {@code User-Agent} que
 * identifique a la aplicacion; se agrega con {@link ClientHeaderParam}. Para
 * uso intensivo conviene desplegar una instancia propia de Nominatim.</p>
 *
 * <p>Endpoint: {@code GET /reverse?lat=..&lon=..&format=jsonv2&addressdetails=1}</p>
 */
@RegisterRestClient(configKey = "nominatim")
@ClientHeaderParam(name = "User-Agent", value = "tracking-service/1.0 (kcd-quarkus-acl)")
@Path("/reverse")
public interface NominatimApi {

    /**
     * Invoca la geocodificacion inversa de Nominatim.
     *
     * @param lat            latitud
     * @param lon            longitud
     * @param format         formato de salida (se usa "jsonv2")
     * @param addressDetails 1 para incluir el desglose de la direccion
     * @return la respuesta cruda del proveedor
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    NominatimResponse reverse(@QueryParam("lat") double lat,
                              @QueryParam("lon") double lon,
                              @QueryParam("format") String format,
                              @QueryParam("addressdetails") int addressDetails);
}
