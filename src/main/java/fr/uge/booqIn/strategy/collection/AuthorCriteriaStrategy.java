package fr.uge.booqIn.strategy.collection;

import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;

import java.util.HashSet;
import java.util.Set;

public class AuthorCriteriaStrategy implements CollectionCriteriaStrategy {
  private Set<Author> authors = new HashSet<>();

  public AuthorCriteriaStrategy(Set<Author> authors) {
    this.authors = authors;
  }


  @Override
  public boolean satisfy(Book book) {
    return book.getAuthors().containsAll(authors);
  }
}
