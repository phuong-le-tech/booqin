package fr.uge.booqIn.event.publishable;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.event.BooqInEvent;

public record BookAvailableEvent(Long userId, String bookTitle) implements BooqInEvent {
}
