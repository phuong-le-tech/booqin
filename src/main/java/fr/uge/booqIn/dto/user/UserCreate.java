package fr.uge.booqIn.dto.user;

import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.enums.UserRole;

public record UserCreate (
   String firstName,
   String lastName,
   String email,
   String password,
   Address address,
   UserRole role
){
}
