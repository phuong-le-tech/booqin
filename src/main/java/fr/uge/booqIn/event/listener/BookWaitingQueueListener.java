package fr.uge.booqIn.event.listener;

import fr.uge.booqIn.event.publishable.BookAvailableEvent;
import fr.uge.booqIn.service.interfaces.BookWaitingQueueServiceInterface;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class BookWaitingQueueListener {
  private static final Logger logger = Logger.getLogger(BookWaitingQueueListener.class.getName());
  private final BookWaitingQueueServiceInterface bookWaitingQueueService;

  public BookWaitingQueueListener(BookWaitingQueueServiceInterface bookWaitingQueueService) {
    this.bookWaitingQueueService = bookWaitingQueueService;
  }

  @EventListener
  public void handleOnBookAvailable(BookAvailableEvent event) {
    bookWaitingQueueService.processNextInQueue(event.bookId());
  }
}
