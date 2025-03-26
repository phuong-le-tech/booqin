package fr.uge.booqIn.event.publishable;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.event.BooqInEvent;

public record BookAddedEvent(
        Long bookId,
        boolean isNewBook
) implements BooqInEvent {
    @Override
    public String toString() {
        return "ApplicationEvent :: Book added " + bookId + " | " + isNewBook;
    }
}
