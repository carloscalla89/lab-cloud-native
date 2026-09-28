package com.labcloudnative.trackingservice.infrastructure.geocoding;

import com.labcloudnative.trackingservice.domain.exception.GeocodingException;
import com.labcloudnative.trackingservice.domain.model.Address;
import com.labcloudnative.trackingservice.domain.model.Coordinates;
import com.labcloudnative.trackingservice.domain.port.ReverseGeocodingPort;
import com.labcloudnative.trackingservice.infrastructure.geocoding.nominatim.NominatimApi;
import com.labcloudnative.trackingservice.infrastructure.geocoding.nominatim.NominatimResponse;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Adaptador del Anti-Corruption Layer para geocodificacion inversa usando
 * <b>Nominatim (OpenStreetMap)</b>, una API gratuita y sin clave.
 *
 * <p>Implementa el mismo puerto del dominio {@link ReverseGeocodingPort} que
 * el adaptador de Google, traduciendo la respuesta cruda de Nominatim
 * ({@link NominatimResponse}) al modelo limpio {@link Address} y convirtiendo
 * los fallos tecnicos en {@link GeocodingException} del dominio.</p>
 *
 * <p>Se activa cuando {@code geocoding.provider=nominatim} o cuando la
 * propiedad no esta definida ({@code enableIfMissing = true}), por lo que es
 * el proveedor por defecto. La seleccion ocurre en tiempo de build.</p>
 */
@ApplicationScoped
@IfBuildProperty(name = "geocoding.provider", stringValue = "nominatim", enableIfMissing = true)
public class NominatimReverseGeocodingAdapter implements ReverseGeocodingPort {

    private static final String FORMAT_JSON_V2 = "jsonv2";
    private static final int WITH_ADDRESS_DETAILS = 1;

    private final NominatimApi nominatimApi;

    public NominatimReverseGeocodingAdapter(@RestClient NominatimApi nominatimApi) {
        this.nominatimApi = nominatimApi;
    }

    @Override
    public Address reverseGeocode(Coordinates coordinates) {
        NominatimResponse response;
        try {
            response = nominatimApi.reverse(
                    coordinates.getLatitude(),
                    coordinates.getLongitude(),
                    FORMAT_JSON_V2,
                    WITH_ADDRESS_DETAILS);
        } catch (RuntimeException e) {
            // Errores de red, timeouts o codigos HTTP de error -> concepto de dominio.
            throw new GeocodingException("Fallo al invocar la API de Nominatim", e);
        }

        validate(response);
        return translate(response);
    }

    /**
     * Verifica que la respuesta sea utilizable; si no, lanza una excepcion de dominio.
     */
    private void validate(NominatimResponse response) {
        if (response == null) {
            throw new GeocodingException("Respuesta vacia de Nominatim");
        }
        if (response.error != null && !response.error.isBlank()) {
            throw new GeocodingException("Nominatim respondio con error: " + response.error);
        }
        if (response.displayName == null || response.displayName.isBlank()) {
            throw new GeocodingException("Nominatim no devolvio resultados para las coordenadas");
        }
    }

    /**
     * Traduce la respuesta de Nominatim al objeto de valor {@link Address} del dominio.
     */
    private Address translate(NominatimResponse response) {
        NominatimResponse.AddressDetails details = response.address;

        String street = null;
        String city = null;
        String postalCode = null;
        String country = null;

        if (details != null) {
            street = joinStreet(details.road, details.houseNumber);
            city = firstNonBlank(details.city, details.town, details.village, details.municipality);
            postalCode = details.postcode;
            country = details.country;
        }

        String placeId = response.placeId != null ? String.valueOf(response.placeId) : null;

        return new Address(
                response.displayName,
                street,
                city,
                postalCode,
                country,
                placeId);
    }

    /**
     * Combina via y numero en una unica cadena de calle, tolerando nulos.
     */
    private String joinStreet(String road, String houseNumber) {
        if (road == null) {
            return null;
        }
        if (houseNumber == null || houseNumber.isBlank()) {
            return road;
        }
        return road + " " + houseNumber;
    }

    /**
     * Devuelve el primer valor no nulo ni vacio, o {@code null} si no hay ninguno.
     */
    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
