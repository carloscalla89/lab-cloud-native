package com.kcd.trackingservice.domain.model;

import java.util.Objects;

/**
 * Objeto de valor (Value Object) que representa una direccion ya normalizada.
 *
 * <p>Este es el modelo <b>limpio</b> del dominio para una direccion. El
 * Anti-Corruption Layer traduce la respuesta cruda de Google Maps a este
 * tipo, de modo que el formato y las particularidades del proveedor externo
 * nunca penetran en el dominio.</p>
 *
 * <p>Solo {@code formattedAddress} es obligatorio; el resto de campos pueden
 * ser nulos porque no toda direccion devuelve todos los componentes.</p>
 */
public final class Address {

    private final String formattedAddress;
    private final String street;
    private final String city;
    private final String postalCode;
    private final String country;
    private final String placeId;

    public Address(String formattedAddress,
                   String street,
                   String city,
                   String postalCode,
                   String country,
                   String placeId) {
        if (formattedAddress == null || formattedAddress.isBlank()) {
            throw new IllegalArgumentException("formattedAddress es obligatorio");
        }
        this.formattedAddress = formattedAddress;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
        this.placeId = placeId;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public String getPlaceId() {
        return placeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Address that)) {
            return false;
        }
        return Objects.equals(formattedAddress, that.formattedAddress)
                && Objects.equals(street, that.street)
                && Objects.equals(city, that.city)
                && Objects.equals(postalCode, that.postalCode)
                && Objects.equals(country, that.country)
                && Objects.equals(placeId, that.placeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formattedAddress, street, city, postalCode, country, placeId);
    }
}
