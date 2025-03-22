package fr.uge.booqIn.event;

import fr.uge.booqIn.entity.Book;

import java.time.Instant;

public record BookCreatedEvent(
        Object source,
        Book book,
        Instant timestamp,
        String eventId
) implements DomainEvent {

    public BookCreatedEvent(Object source, Book book) {
        this(
                source,
                book,
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

    public Book getBook() {
        return book;
    }
}