package fr.uge.booqIn.strategy.collection;

import fr.uge.booqIn.entity.Book;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class PublicationDateCriteriaStrategy implements CollectionCriteriaStrategy {
  private final int maxPublicationDate;

  // TODO: à changer par une meilleure façon de faire
  private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
          DateTimeFormatter.ISO_LOCAL_DATE, // yyyy-MM-dd
          DateTimeFormatter.ofPattern("dd/MM/yyyy"),
          DateTimeFormatter.ofPattern("MM/dd/yyyy"),
          DateTimeFormatter.ofPattern("yyyy/MM/dd"),
          DateTimeFormatter.ofPattern("yyyy-MM"),
          DateTimeFormatter.ofPattern("yyyy")
  );

  // get the publication date type/format
  public PublicationDateCriteriaStrategy(int maxPublicationDate) {
    this.maxPublicationDate = maxPublicationDate;
  }

  @Override
  public boolean satisfy(Book book) {
    if (book.getPublicationDate() == null  || book.getPublicationDate().isEmpty()) {
      return false;
    }
    for (DateTimeFormatter formatter : DATE_FORMATTERS) {
      try {
        LocalDate publicationDate = LocalDate.parse(book.getPublicationDate(), formatter);
        return publicationDate.getYear() <= maxPublicationDate;
      } catch (DateTimeParseException ignored) {
        // do nothing, if parsing failed, try the next date format
      }
    }
    return false; // no date format matched/founded, does not satisfy the criteria
  }
}
