package fr.uge.booqIn.event.publishable.bookqueue;

import fr.uge.booqIn.event.BooqInEvent;

public record BookQueueJoinedEvent(Long userId, String bookTitle, Integer position) implements BooqInEvent {
}