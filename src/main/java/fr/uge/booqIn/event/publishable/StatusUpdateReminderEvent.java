package fr.uge.booqIn.event.publishable;

import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.event.BooqInEvent;

public record StatusUpdateReminderEvent(BookLending bookLending) implements BooqInEvent {
  
  @Override
  public String toString() {
    return "ApplicationEvent :: Status Update Reminder for book '" +
            bookLending.getBook().getTitle() +
            "' with status " + bookLending.getStatus();
  }
}