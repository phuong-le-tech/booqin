package fr.uge.booqIn.event.publishable.bookqueue;

import fr.uge.booqIn.event.BooqInEvent;

public record BookQueuePositionUpdatedEvent(Long userId, Long bookId, Integer oldPosition, Integer newPosition) implements BooqInEvent {
}