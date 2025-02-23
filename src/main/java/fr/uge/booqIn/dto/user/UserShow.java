package fr.uge.booqIn.dto.user;

import fr.uge.booqIn.entity.Address;

public record UserShow (
  Long id,
  String firstName,
  String lastName,
  String email,
  Address address
  ) {
}
