package fr.uge.booqIn.event.publishable.bookqueue;

import fr.uge.booqIn.event.BooqInEvent;

public record BookQueueReservationEvent(Long userId, String bookTitle) implements BooqInEvent {
}
