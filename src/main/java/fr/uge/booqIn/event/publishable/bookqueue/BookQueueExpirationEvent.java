package fr.uge.booqIn.event.publishable.bookqueue;

import fr.uge.booqIn.event.BooqInEvent;

public record BookQueueExpirationEvent(Long userId, String bookTitle) implements BooqInEvent {
}