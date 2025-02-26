package fr.uge.booqIn.dto.user;

import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.enums.UserRole;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String password,
        Address address,
        UserRole role,
        String createdAt,
        String updatedAt,
        Cart cart
) { }