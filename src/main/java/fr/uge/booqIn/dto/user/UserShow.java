package fr.uge.booqIn.dto.user;

import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.enums.UserRole;

import java.util.List;

public record UserShow (
  Long id,
  String firstName,
  String lastName,
  String email,
  Address address,
  List<BookCollection> collections,
  UserRole role
  ) {
}
