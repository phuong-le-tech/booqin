package fr.uge.booqIn.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public record Person(String firstName, String lastName, String email, Address address) {
    public Person {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || address == null) {
            throw new IllegalArgumentException("Person fields must not be empty");
        }
    }
}
