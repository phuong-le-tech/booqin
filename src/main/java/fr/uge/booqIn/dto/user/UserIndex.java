package fr.uge.booqIn.dto.user;

import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.enums.UserRole;

public record UserIndex(
        Long id, String firstName, String lastName,
        String email, Address address, UserRole role
) {
  public UserIndex() {
    this(0L, "", "", "", null, UserRole.USER);
  }
}
