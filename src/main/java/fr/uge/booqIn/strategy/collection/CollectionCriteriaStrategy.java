package fr.uge.booqIn.strategy.collection;

import fr.uge.booqIn.entity.Book;

@FunctionalInterface
public interface CollectionCriteriaStrategy {
  /**
   * Check if a book satisfies the criteria
   * @param book The book to check
   * @return true if the book satisfies the criteria, false otherwise
   */
  boolean satisfy(Book book);

  /**
   * Combine this criteria with another one using AND logic
   * @param otherCriteria The other criteria to combine with
   * @return A new criteria strategy that combines both
   */
  default CollectionCriteriaStrategy combineCriteria(CollectionCriteriaStrategy otherCriteria) {
    return book -> this.satisfy(book) && otherCriteria.satisfy(book);
  }
}