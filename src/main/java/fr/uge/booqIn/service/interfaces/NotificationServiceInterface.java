package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.notification.BookNotificationDTO;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationServiceInterface {
  boolean addToNotificationQueue(Long bookId, Long userId);
  void processNextInQueue(Long bookId);
  void processExpiredNotifications();
  boolean reserveNotifiedBook(Long notificationId, Long userId);
  List<BookNotificationDTO> getActiveNotifications(Long userId);
  void sendBookAvailableNotification(User user, Book book, LocalDateTime expiresAt);
}

