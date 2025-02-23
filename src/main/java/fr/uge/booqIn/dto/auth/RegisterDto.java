package fr.uge.booqIn.dto.auth;

import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.enums.UserRole;

public record RegisterDto(
        String firstName,
        String lastName,
        String email,
        String password,
        Address address,
        UserRole role
) {
}
