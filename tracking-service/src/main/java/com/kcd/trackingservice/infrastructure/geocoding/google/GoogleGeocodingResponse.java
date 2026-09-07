package com.kcd.trackingservice.infrastructure.geocoding.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO externo: respuesta cruda de la API de geocodificacion de Google Maps.
 *
 * <p>Estas clases modelan el formato "tal cual" del proveedor externo y viven
 * <b>exclusivamente</b> dentro del Anti-Corruption Layer. Nunca deben salir de
 * la capa de infraestructura: el adaptador las traduce al modelo limpio del
 * dominio ({@code Address}).</p>
 *
 * <p>{@code @JsonIgnoreProperties(ignoreUnknown = true)} hace el parseo robusto
 * ante cambios o campos adicionales del proveedor.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleGeocodingResponse {

    /** Estado de la respuesta: OK, ZERO_RESULTS, REQUEST_DENIED, etc. */
    @JsonProperty("status")
    public String status;

    /** Mensaje de error opcional que devuelve Google ante ciertos estados. */
    @JsonProperty("error_message")
    public String errorMessage;

    /** Lista de resultados; el primero es el mas relevante. */
    @JsonProperty("results")
    public List<Result> results;

    /** Un resultado individual de geocodificacion. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        @JsonProperty("formatted_address")
        public String formattedAddress;

        @JsonProperty("place_id")
        public String placeId;

        @JsonProperty("address_components")
        public List<AddressComponent> addressComponents;
    }

    /** Componente de una direccion (calle, ciudad, pais, etc.). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressComponent {

        @JsonProperty("long_name")
        public String longName;

        @JsonProperty("short_name")
        public String shortName;

        /** Tipos del componente: route, locality, country, postal_code, etc. */
        @JsonProperty("types")
        public List<String> types;
    }
}
