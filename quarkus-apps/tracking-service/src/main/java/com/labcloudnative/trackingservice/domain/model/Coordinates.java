package com.labcloudnative.trackingservice.domain.model;

import com.labcloudnative.trackingservice.domain.exception.InvalidCoordinatesException;

import java.util.Objects;

/**
 * Objeto de valor (Value Object) que representa unas coordenadas geograficas.
 *
 * <p>Es inmutable y valida sus invariantes en la construccion: la latitud
 * debe estar en [-90, 90] y la longitud en [-180, 180]. Forma parte del
 * nucleo del dominio y no depende de ningun framework.</p>
 */
public final class Coordinates {

    private final double latitude;
    private final double longitude;

    /**
     * Crea unas coordenadas validando los rangos geograficos.
     *
     * @param latitude  latitud en grados [-90, 90]
     * @param longitude longitud en grados [-180, 180]
     * @throws InvalidCoordinatesException si algun valor esta fuera de rango
     */
    public Coordinates(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new InvalidCoordinatesException("La latitud debe estar entre -90 y 90: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new InvalidCoordinatesException("La longitud debe estar entre -180 y 180: " + longitude);
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Coordinates that)) {
            return false;
        }
        return Double.compare(latitude, that.latitude) == 0
                && Double.compare(longitude, that.longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return latitude + "," + longitude;
    }
}
