package fr.uge.booqIn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Embeddable
public record Address(
        @NotBlank(message = "Street number is required")
        @Size(max = 10, message = "Street number cannot exceed 10 characters")
        @Column(name = "street_number")
        String streetNumber,

        @NotBlank(message = "Street name is required")
        @Size(max = 100, message = "Street name cannot exceed 100 characters")
        @Column(name = "street_name")
        String streetName,

        @NotBlank(message = "City is required")
        @Size(max = 50, message = "City cannot exceed 50 characters")
        @Column(name = "city")
        String city,

        @NotBlank(message = "Postal code is required")
        @Pattern(regexp = "^[0-9]{5}(?:-[0-9]{4})?$", message = "Invalid postal code format")
        @Column(name = "postal_code")
        String postalCode
) {
    public Address {
        if (streetNumber == null || streetName == null || city == null || postalCode == null) {
            throw new IllegalArgumentException("Address fields must not be null");
        }

        if (streetNumber.isBlank() || streetName.isBlank() || city.isBlank() || postalCode.isBlank()) {
            throw new IllegalArgumentException("Address fields must not be empty");
        }

        if (!postalCode.matches("^[0-9]{5}(?:-[0-9]{4})?$")) {
            throw new IllegalArgumentException("Invalid postal code format");
        }
    }

    public Address() {
        this("", "", "", "");
    }

    @Override
    public String toString() {
        return streetNumber + " " + streetName + ", " + city + " " + postalCode;
    }
}