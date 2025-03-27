package fr.uge.booqIn.event.publishable.bookqueue;

import fr.uge.booqIn.entity.BookWaitingQueue;
import fr.uge.booqIn.event.BooqInEvent;

public record BookQueueNotificationEvent(Long userId, Long bookId, Long queueId, BookWaitingQueue queueEntry) implements BooqInEvent {
}