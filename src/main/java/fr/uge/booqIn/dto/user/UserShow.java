package fr.uge.booqIn.dto.user;

import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.enums.UserRole;

import java.util.List;

public record UserShow (
        Long id,
        String firstName,
        String lastName,
        String email,
        Address address,
        List<BookCollectionIndex> collections,
        UserRole role
  ) {
}
