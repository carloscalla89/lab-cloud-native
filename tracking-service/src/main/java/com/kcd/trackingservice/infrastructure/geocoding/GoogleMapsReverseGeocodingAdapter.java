package com.kcd.trackingservice.infrastructure.geocoding;

import com.kcd.trackingservice.domain.exception.GeocodingException;
import com.kcd.trackingservice.domain.model.Address;
import com.kcd.trackingservice.domain.model.Coordinates;
import com.kcd.trackingservice.domain.port.ReverseGeocodingPort;
import com.kcd.trackingservice.infrastructure.geocoding.google.GoogleGeocodingResponse;
import com.kcd.trackingservice.infrastructure.geocoding.google.GoogleMapsGeocodingApi;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador del Anti-Corruption Layer para la geocodificacion inversa.
 *
 * <p>Es la pieza central del ACL: implementa el puerto del dominio
 * {@link ReverseGeocodingPort} usando la API externa de Google Maps, y
 * <b>traduce</b> el modelo crudo del proveedor ({@link GoogleGeocodingResponse})
 * al modelo limpio del dominio ({@link Address}).</p>
 *
 * <p>Tambien aisla los fallos: cualquier error tecnico (excepcion de red,
 * codigo HTTP, o estado distinto de OK) se convierte en una
 * {@link GeocodingException} del dominio, evitando que detalles del proveedor
 * se filtren hacia las capas internas.</p>
 *
 * <p>Se activa unicamente cuando {@code geocoding.provider=google}. El
 * proveedor por defecto es Nominatim (gratuito). La seleccion ocurre en
 * tiempo de build.</p>
 */
@ApplicationScoped
@IfBuildProperty(name = "geocoding.provider", stringValue = "google")
public class GoogleMapsReverseGeocodingAdapter implements ReverseGeocodingPort {

    // Constantes con los tipos de componente de direccion que usa Google.
    private static final String TYPE_STREET_NUMBER = "street_number";
    private static final String TYPE_ROUTE = "route";
    private static final String TYPE_LOCALITY = "locality";
    private static final String TYPE_POSTAL_TOWN = "postal_town";
    private static final String TYPE_ADMIN_AREA_2 = "administrative_area_level_2";
    private static final String TYPE_POSTAL_CODE = "postal_code";
    private static final String TYPE_COUNTRY = "country";

    private final GoogleMapsGeocodingApi geocodingApi;
    private final String apiKey;

    public GoogleMapsReverseGeocodingAdapter(
            @RestClient GoogleMapsGeocodingApi geocodingApi,
            @ConfigProperty(name = "google.maps.api-key") String apiKey) {
        this.geocodingApi = geocodingApi;
        this.apiKey = apiKey;
    }

    @Override
    public Address reverseGeocode(Coordinates coordinates) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new GeocodingException("La clave de API de Google Maps no esta configurada (GOOGLE_MAPS_API_KEY)");
        }

        String latlng = coordinates.getLatitude() + "," + coordinates.getLongitude();

        GoogleGeocodingResponse response;
        try {
            // Llamada a la API externa.
            response = geocodingApi.reverseGeocode(latlng, apiKey);
        } catch (RuntimeException e) {
            // Errores de red, timeouts o codigos HTTP de error -> concepto de dominio.
            throw new GeocodingException("Fallo al invocar la API de Google Maps", e);
        }

        validateStatus(response);

        // Se toma el primer resultado (el mas relevante) y se traduce al dominio.
        GoogleGeocodingResponse.Result result = response.results.get(0);
        return translate(result);
    }

    /**
     * Verifica que la respuesta del proveedor sea utilizable; si no, lanza
     * una excepcion de dominio con un mensaje claro.
     */
    private void validateStatus(GoogleGeocodingResponse response) {
        if (response == null || response.status == null) {
            throw new GeocodingException("Respuesta vacia o sin estado de Google Maps");
        }
        if (!"OK".equals(response.status)) {
            String detail = response.errorMessage != null ? " - " + response.errorMessage : "";
            throw new GeocodingException("Google Maps respondio con estado " + response.status + detail);
        }
        if (response.results == null || response.results.isEmpty()) {
            throw new GeocodingException("Google Maps no devolvio resultados para las coordenadas");
        }
    }

    /**
     * Traduce un resultado de Google al objeto de valor {@link Address} del dominio,
     * extrayendo los componentes relevantes por su tipo.
     */
    private Address translate(GoogleGeocodingResponse.Result result) {
        List<GoogleGeocodingResponse.AddressComponent> components =
                result.addressComponents != null ? result.addressComponents : List.of();

        // La "calle" se compone de numero + via cuando ambos estan presentes.
        String streetNumber = findComponent(components, TYPE_STREET_NUMBER);
        String route = findComponent(components, TYPE_ROUTE);
        String street = joinStreet(streetNumber, route);

        // La ciudad puede venir como locality, postal_town o area administrativa nivel 2.
        String city = Optional.ofNullable(findComponent(components, TYPE_LOCALITY))
                .or(() -> Optional.ofNullable(findComponent(components, TYPE_POSTAL_TOWN)))
                .or(() -> Optional.ofNullable(findComponent(components, TYPE_ADMIN_AREA_2)))
                .orElse(null);

        String postalCode = findComponent(components, TYPE_POSTAL_CODE);
        String country = findComponent(components, TYPE_COUNTRY);

        return new Address(
                result.formattedAddress,
                street,
                city,
                postalCode,
                country,
                result.placeId);
    }

    /**
     * Busca el {@code long_name} del primer componente que incluya el tipo dado.
     *
     * @return el valor del componente, o {@code null} si no existe
     */
    private String findComponent(List<GoogleGeocodingResponse.AddressComponent> components, String type) {
        return components.stream()
                .filter(c -> c.types != null && c.types.contains(type))
                .map(c -> c.longName)
                .findFirst()
                .orElse(null);
    }

    /**
     * Combina numero y via en una unica cadena de calle, tolerando nulos.
     */
    private String joinStreet(String streetNumber, String route) {
        if (route == null) {
            return streetNumber; // puede ser null
        }
        if (streetNumber == null) {
            return route;
        }
        return route + " " + streetNumber;
    }
}
