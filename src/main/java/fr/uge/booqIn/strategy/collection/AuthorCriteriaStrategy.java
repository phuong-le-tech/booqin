package fr.uge.booqIn.strategy.collection;

import fr.uge.booqIn.entity.Book;

public class AuthorCriteriaStrategy implements CollectionCriteriaStrategy {
  private final String authorName;

  public AuthorCriteriaStrategy(String authorName) {
    this.authorName = authorName;
  }

  @Override
  public boolean satisfy(Book book) {
    return book.getAuthor().equals(authorName);
  }
}
