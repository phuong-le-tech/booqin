package fr.uge.booqIn.event.publishable;

import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.event.BooqInEvent;

public record LateBookReturnEvent(BookLending bookLending) implements BooqInEvent {
  
  @Override
  public String toString() {
    return "ApplicationEvent :: Late Book Return for book '" +
            bookLending.getBook().getTitle() +
            "' borrowed by " +
            bookLending.getBorrower().getFullName();
  }
}