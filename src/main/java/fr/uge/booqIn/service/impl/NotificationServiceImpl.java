package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.notification.BookNotificationDTO;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookNotificationQueue;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.NotificationStatus;
import fr.uge.booqIn.event.publishable.BookNotAvailableEvent;
import fr.uge.booqIn.event.publishable.BookReturnedEvent;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.NotificationMappeur;
import fr.uge.booqIn.repository.BookNotificationQueueRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.NotificationServiceInterface;
import jakarta.transaction.Transactional;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationServiceInterface {

  private final BookNotificationQueueRepository notificationQueueRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final BookLendingInterface bookLendingService;
  private final NotificationMappeur notificationMappeur;

  public NotificationServiceImpl(
      BookNotificationQueueRepository notificationQueueRepository,
      BookRepository bookRepository,
      UserRepository userRepository,
      BookLendingInterface bookLendingService, NotificationMappeur notificationMappeur) {
    this.notificationQueueRepository = notificationQueueRepository;
    this.bookRepository = bookRepository;
    this.userRepository = userRepository;
    this.bookLendingService = bookLendingService;
    this.notificationMappeur = notificationMappeur;
  }

  @Override
  public boolean addToNotificationQueue(Long bookId, Long userId) {
    List<NotificationStatus> activeStatuses = Arrays.asList(
        NotificationStatus.WAITING,
        NotificationStatus.NOTIFIED);

    if (notificationQueueRepository.existsByBookIdAndUserIdAndStatusIn(bookId, userId, activeStatuses)) {
      return false;
    }

    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new ResourceNotFoundException("Livre non trouvé avec l'ID: " + bookId));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + userId));

    int position = notificationQueueRepository.findMaxPositionByBookId(bookId)
        .orElse(0) + 1;

    BookNotificationQueue notification = new BookNotificationQueue(book, user, position);

    notificationQueueRepository.save(notification);
    return true;
  }

  @Override
  public void processNextInQueue(Long bookId) {

    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new IllegalArgumentException("Livre non trouvé avec l'ID: " + bookId));

    var available = bookLendingService.findBookLendingByBookId(bookId);
    if (available.isPresent()) {
      return;
    }

    Optional<BookNotificationQueue> nextInQueue = notificationQueueRepository
        .findFirstByBookIdAndStatusOrderByPositionAsc(bookId, NotificationStatus.WAITING);

    if (nextInQueue.isPresent()) {
      BookNotificationQueue notification = nextInQueue.get();

      notification.setStatus(NotificationStatus.NOTIFIED);
      notification.setNotifiedAt(LocalDateTime.now());
      notification.setReservationExpiresAt(LocalDateTime.now().plusHours(1));
      notificationQueueRepository.save(notification);

    }
  }

  @Override
  @Scheduled(fixedRate = 5 * 60 * 1000)
  @Transactional
  public void processExpiredNotifications() {
    List<BookNotificationQueue> expiredNotifications = notificationQueueRepository
        .findExpiredNotifications(LocalDateTime.now());

    for (BookNotificationQueue notification : expiredNotifications) {
      notification.setStatus(NotificationStatus.EXPIRED);
      notificationQueueRepository.save(notification);

      processNextInQueue(notification.getBook().getId());
    }
  }

  @Override
  @Transactional
  public boolean reserveNotifiedBook(Long notificationId, Long userId) {
    Optional<BookNotificationQueue> optNotification = notificationQueueRepository.findById(notificationId);

    if (optNotification.isEmpty()) {
      return false;
    }

    BookNotificationQueue notification = optNotification.get();

    if (!notification.getUser().getId().equals(userId)) {
      return false;
    }

    if (notification.getStatus() != NotificationStatus.NOTIFIED) {
      return false;
    }

    if (notification.getReservationExpiresAt().isBefore(LocalDateTime.now())) {
      notification.setStatus(NotificationStatus.EXPIRED);
      notificationQueueRepository.save(notification);
      processNextInQueue(notification.getBook().getId());
      return false;
    }

    boolean reserved = bookLendingService.reserveBook(notification.getBook().getId(), userId);
    if (reserved) {
      notification.setStatus(NotificationStatus.RESERVED);
      notificationQueueRepository.save(notification);
      return true;
    }
    return false;
  }

  @Override
  public List<BookNotificationDTO> getActiveNotifications(Long userId) {

    List<NotificationStatus> activeStatuses = Arrays.asList(
        NotificationStatus.WAITING,
        NotificationStatus.NOTIFIED);

    List<BookNotificationQueue> notifications = notificationQueueRepository
        .findByUserIdAndStatusInOrderByCreatedAtDesc(userId, activeStatuses);

    return notifications.stream()
        .map(notificationMappeur::mapToBookNotificationDTO)
        .collect(Collectors.toList());
  }

  @Override
  public void sendBookAvailableNotification(User user, Book book, LocalDateTime expiresAt) {

  }
  
  @EventListener
  public void handleBookReturnedEvent(BookReturnedEvent event) {
    processNextInQueue(event.bookId());
  }
  
  @EventListener
  public void handleBookNotAvailableEvent(BookNotAvailableEvent event) {
    
    boolean added = addToNotificationQueue(event.bookId(), event.userId());
  }
}