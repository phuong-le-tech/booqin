package fr.uge.booqIn.scheduler;

import fr.uge.booqIn.service.interfaces.BookWaitingQueueServiceInterface;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class BookWaitingQueueScheduler {
  private static final Logger logger = Logger.getLogger(BookWaitingQueueScheduler.class.getName());
  private final BookWaitingQueueServiceInterface bookWaitingQueueService;

  public BookWaitingQueueScheduler(BookWaitingQueueServiceInterface bookWaitingQueueService) {
    this.bookWaitingQueueService = bookWaitingQueueService;
  }

  @Scheduled(fixedRate = 5 * 60 * 1000)
  public void processExpiredNotifications() {
    bookWaitingQueueService.processExpiredNotifications();
  }
}
