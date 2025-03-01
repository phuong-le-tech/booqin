package fr.uge.booqIn.strategy.collection;

import fr.uge.booqIn.entity.Book;

import java.util.Calendar;
import java.util.Date;

public class PublicationDateCriteriaStrategy implements CollectionCriteriaStrategy {
  private final int maxPublicationYear;

  public PublicationDateCriteriaStrategy(int maxPublicationYear) {
    this.maxPublicationYear = maxPublicationYear;
  }

  @Override
  public boolean satisfy(Book book) {
    if (book.getPublicationDate() == null) {
      return false;
    }

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(book.getPublicationDate());
    int publicationYear = calendar.get(Calendar.YEAR);

    return publicationYear <= maxPublicationYear;
  }
}