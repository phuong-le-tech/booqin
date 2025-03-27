package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.notification.BookNotificationDTO;

import java.util.List;

public interface BookWaitingQueueServiceInterface {
  boolean addToWaitingQueue(Long bookId, Long userId);

  void processNextInQueue(Long bookId);

  void processExpiredNotifications();

  boolean reserveBookFromQueue(Long queueId, Long userId);

  boolean cancelQueueEntry(Long queueId, Long userId);

  List<BookNotificationDTO> getUserActiveQueues(Long userId);

  int getUserQueuePosition(Long bookId, Long userId);
}

