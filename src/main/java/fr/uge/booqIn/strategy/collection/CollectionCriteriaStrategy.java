package fr.uge.booqIn.strategy.collection;

import fr.uge.booqIn.entity.Book;

@FunctionalInterface
public interface CollectionCriteriaStrategy {
  //boolean isSatisfiedBy(String criteria);
  boolean satisfy(Book book);

  default CollectionCriteriaStrategy combineCriteria(CollectionCriteriaStrategy otherCriteria) {
    return book -> this.satisfy(book) && otherCriteria.satisfy(book);
  }
}
