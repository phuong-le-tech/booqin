package fr.uge.booqIn.event.publishable;

import fr.uge.booqIn.event.BooqInEvent;

public record BookNotAvailableEvent(
        Long bookId,
        Long userId
) implements BooqInEvent {
  @Override
  public String toString() {
    return "ApplicationEvent :: Book with ID " + bookId + " is not available for user with ID " + userId;
  }

}