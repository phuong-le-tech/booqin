package fr.uge.booqIn.event.publishable;


import fr.uge.booqIn.event.BooqInEvent;

public record BookReturnedEvent(Long bookId) implements BooqInEvent {

  @Override
  public String toString() {
    return "ApplicationEvent :: Return Book with ID " + bookId;
  }
}