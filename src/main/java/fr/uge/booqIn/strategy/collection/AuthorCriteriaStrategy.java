package fr.uge.booqIn.strategy.collection;

import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;

import java.util.HashSet;
import java.util.Set;

public class AuthorCriteriaStrategy implements CollectionCriteriaStrategy {
  private final String authorName;

  public AuthorCriteriaStrategy(String authorName) {
    this.authorName = authorName.toLowerCase();
  }


  @Override
  public boolean satisfy(Book book) {
    return book.getAuthors().stream()
            .anyMatch(this::matchesAuthor);
  }

  private boolean matchesAuthor(Author author) {
    String fullName = (author.getFirstName() + " " + author.getLastName()).toLowerCase();
    if (fullName.contains(authorName)) {
      return true;
    }
    if (author.getLastName().toLowerCase().contains(authorName)) {
      return true;
    }
    return author.getFirstName().toLowerCase().contains(authorName);
  }
}
