package fr.uge.booqIn.dto.auth;

import fr.uge.booqIn.entity.Address;

public record UserMeDto(long id, String firstName, String lastName, String email, String role, Address address) {
}
