package fr.uge.booqIn.event.publishable.bookqueue;

import fr.uge.booqIn.event.BooqInEvent;

public record BookQueueCancellationEvent(Long userId, String bookTitle) implements BooqInEvent {
}