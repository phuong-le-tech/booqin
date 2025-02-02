package fr.uge.booqIn.dto.model;

import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.enums.UserRole;

public record UserDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String password,
        Address address,
        UserRole role,
        String createdAt,
        String updatedAt
) { }