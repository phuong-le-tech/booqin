package fr.uge.booqIn.dto.user;

import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.ToLendBook;
import fr.uge.booqIn.enums.UserRole;

import java.util.List;
import java.util.Set;

public record UserShow (
  Long id,
  String firstName,
  String lastName,
  String email,
  Address address,
  Set<ToLendBook> booksToLend,
  List<BookCollection> collections,
  UserRole role
  ) {
}
