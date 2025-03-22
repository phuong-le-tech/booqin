package fr.uge.booqIn.event;

import java.time.Instant;

public record BookNotAvailableEvent(
        Object source,
        Long bookId,
        Long userId,
        Instant timestamp,
        String eventId
) implements DomainEvent {

  public BookNotAvailableEvent(Object source, Long bookId, Long userId) {
    this(
            source,
            bookId,
            userId,
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