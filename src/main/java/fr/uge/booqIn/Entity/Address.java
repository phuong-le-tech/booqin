package fr.uge.booqIn.Entity;

import jakarta.persistence.Embeddable;

@Embeddable
public record Address(String streetNumber, String streetName, String city, String postalCode) {
    public Address {
        if (streetNumber.isBlank() || streetName.isBlank() || city.isBlank() || postalCode.isBlank()) {
            throw new IllegalArgumentException("Address fields must not be empty");
        }
    }
}
