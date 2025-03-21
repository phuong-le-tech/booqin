package fr.uge.booqIn.event;

import java.time.Instant;

public record BookReturnedEvent(
        Object source,
        Long bookId,
        Instant timestamp,
        String eventId
) implements DomainEvent {

  public BookReturnedEvent(Object source, Long bookId) {
    this(
            source,
            bookId,
            Instant.now(),
            java.util.UUID.randomUUID().toString()
    );
  }

  @Override
  public Object getSource() {
    return source;
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }

  @Override
  public String getEventId() {
    return eventId;
  }

  @Override
  public String getEntityType() {
    return "BOOK";
  }
}