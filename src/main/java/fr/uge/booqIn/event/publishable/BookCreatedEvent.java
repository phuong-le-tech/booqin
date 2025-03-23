package fr.uge.booqIn.event.publishable;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.event.BooqInEvent;

import java.time.Instant;

public record BookCreatedEvent(
        Object source,
        Book book,
        Instant timestamp,
        String eventId
) implements BooqInEvent {

    public Book getBook() {
        return book;
    }
}