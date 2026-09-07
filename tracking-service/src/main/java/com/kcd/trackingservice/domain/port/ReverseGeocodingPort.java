package com.kcd.trackingservice.domain.port;

import com.kcd.trackingservice.domain.exception.GeocodingException;
import com.kcd.trackingservice.domain.model.Address;
import com.kcd.trackingservice.domain.model.Coordinates;

/**
 * Puerto de salida (Port) para la geocodificacion inversa.
 *
 * <p>Define, en lenguaje del dominio, la capacidad de transformar unas
 * coordenadas en una direccion. Es la frontera del Anti-Corruption Layer:
 * el dominio y la aplicacion dependen de esta abstraccion y desconocen por
 * completo que detras hay una API de Google Maps. El adaptador concreto vive
 * en la capa de infraestructura.</p>
 */
public interface ReverseGeocodingPort {

    /**
     * Resuelve una direccion a partir de unas coordenadas.
     *
     * @param coordinates coordenadas a geocodificar
     * @return la direccion resuelta, expresada en el modelo del dominio
     * @throws GeocodingException si el servicio externo falla o no devuelve resultados
     */
    Address reverseGeocode(Coordinates coordinates);
}
