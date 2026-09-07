package com.kcd.trackingservice.infrastructure.geocoding.nominatim;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO externo: respuesta cruda de la API de geocodificacion inversa de
 * Nominatim (OpenStreetMap).
 *
 * <p>Al igual que los DTOs de Google, modela el formato "tal cual" del
 * proveedor y vive <b>exclusivamente</b> dentro del Anti-Corruption Layer.
 * El adaptador lo traduce al modelo limpio del dominio ({@code Address}).</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NominatimResponse {

    /** Identificador del lugar segun OSM. */
    @JsonProperty("place_id")
    public Long placeId;

    /** Direccion completa formateada. */
    @JsonProperty("display_name")
    public String displayName;

    /** Campo presente cuando Nominatim no encuentra resultados. */
    @JsonProperty("error")
    public String error;

    /** Componentes detallados de la direccion. */
    @JsonProperty("address")
    public AddressDetails address;

    /** Desglose de la direccion devuelto por Nominatim. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressDetails {

        @JsonProperty("road")
        public String road;

        @JsonProperty("house_number")
        public String houseNumber;

        // Nominatim puede nombrar la localidad de varias formas segun la zona.
        @JsonProperty("city")
        public String city;

        @JsonProperty("town")
        public String town;

        @JsonProperty("village")
        public String village;

        @JsonProperty("municipality")
        public String municipality;

        @JsonProperty("postcode")
        public String postcode;

        @JsonProperty("country")
        public String country;

        @JsonProperty("country_code")
        public String countryCode;
    }
}
