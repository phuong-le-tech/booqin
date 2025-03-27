package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.notification.BookNotificationDTO;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookWaitingQueue;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.QueueStatus;
import fr.uge.booqIn.event.BooqInEventPublisher;
import fr.uge.booqIn.event.publishable.BookAvailableEvent;
import fr.uge.booqIn.event.publishable.bookqueue.BookQueueCancellationEvent;
import fr.uge.booqIn.event.publishable.bookqueue.BookQueueExpirationEvent;
import fr.uge.booqIn.event.publishable.bookqueue.BookQueueJoinedEvent;
import fr.uge.booqIn.event.publishable.bookqueue.BookQueueReservationEvent;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.NotificationMapper;
import fr.uge.booqIn.repository.BookWaitingQueueRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.BookWaitingQueueServiceInterface;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookWaitingQueueServiceImpl implements BookWaitingQueueServiceInterface {

  private final BookWaitingQueueRepository bookWaitingQueueRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final BookLendingInterface bookLendingService;
  private final NotificationMapper notificationMapper;
  private final BooqInEventPublisher eventPublisher;

  public BookWaitingQueueServiceImpl(
          BookWaitingQueueRepository notificationQueueRepository,
          BookRepository bookRepository,
          UserRepository userRepository,
          BookLendingInterface bookLendingService,
          NotificationMapper notificationMapper,
          BooqInEventPublisher eventPublisher) {
    this.bookWaitingQueueRepository = notificationQueueRepository;
    this.bookRepository = bookRepository;
    this.userRepository = userRepository;
    this.bookLendingService = bookLendingService;
    this.notificationMapper = notificationMapper;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public boolean addToWaitingQueue(Long bookId, Long userId) {
    List<QueueStatus> activeStatuses = Arrays.asList(
            QueueStatus.WAITING,
            QueueStatus.NOTIFIED);

    if (bookWaitingQueueRepository.existsByBookIdAndUserIdAndStatusIn(bookId, userId, activeStatuses)) {
      return false;
    }

    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    var available = bookLendingService.findBookLendingByBookId(bookId);

    if (available.isPresent()) {
      eventPublisher.publishEvent(new BookAvailableEvent(userId, book.getTitle(), book.getId()));
      return true;
    }

    int position = bookWaitingQueueRepository.findMaxPositionByBookId(bookId)
            .orElse(0) + 1;

    BookWaitingQueue queueEntry = new BookWaitingQueue(book, user, position);
    bookWaitingQueueRepository.save(queueEntry);

    eventPublisher.publishEvent(new BookQueueJoinedEvent(userId, book.getTitle(), position));
    return true;
  }

  @Override
  @Transactional
  public void processNextInQueue(Long bookId) {
    var available = bookLendingService.findBookLendingByBookId(bookId);
    if (available.isEmpty()) {
      return;
    }

    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));

    Optional<BookWaitingQueue> nextInQueue = bookWaitingQueueRepository
            .findFirstByBookIdAndStatusOrderByPositionAsc(bookId, QueueStatus.WAITING);
    if (nextInQueue.isPresent()) {
      BookWaitingQueue queueEntry = nextInQueue.get();
      User user = queueEntry.getUser();

      queueEntry.setStatus(QueueStatus.NOTIFIED);
      queueEntry.setNotifiedAt(LocalDateTime.now());
      queueEntry.setReservationExpiresAt(LocalDateTime.now().plusHours(1));
      bookWaitingQueueRepository.save(queueEntry);

      eventPublisher.publishEvent(new BookAvailableEvent(user.getId(), book.getTitle(), book.getId()));
    }
  }

  @Override
  @Scheduled(fixedRate = 5 * 60 * 1000)
  @Transactional
  public void processExpiredNotifications() {
    List<BookWaitingQueue> expiredNotifications = bookWaitingQueueRepository
            .findExpiredNotifications(LocalDateTime.now());

    for (BookWaitingQueue entry : expiredNotifications) {
      entry.setStatus(QueueStatus.EXPIRED);
      bookWaitingQueueRepository.save(entry);
      Book book = entry.getBook();
      User user = entry.getUser();

      eventPublisher.publishEvent(new BookQueueExpirationEvent(user.getId(), book.getTitle()));

      processNextInQueue(book.getId());
    }
  }


  @Override
  @Transactional
  public boolean reserveBookFromQueue(Long queueId, Long userId) {
    Optional<BookWaitingQueue> optEntry = bookWaitingQueueRepository.findById(queueId);

    if (optEntry.isEmpty()) {
      return false;
    }

    BookWaitingQueue queue = optEntry.get();

    if (!queue.getUser().getId().equals(userId)) {
      return false;
    }

    if (queue.getStatus() != QueueStatus.NOTIFIED) {
      return false;
    }

    if (queue.getReservationExpiresAt().isBefore(LocalDateTime.now())) {
      queue.setStatus(QueueStatus.EXPIRED);
      bookWaitingQueueRepository.save(queue);
      processNextInQueue(queue.getBook().getId());
      return false;
    }

    boolean reserved = bookLendingService.reserveBook(queue.getBook().getId(), userId);
    if (reserved) {
      queue.setStatus(QueueStatus.RESERVED);
      bookWaitingQueueRepository.save(queue);
      eventPublisher.publishEvent(new BookQueueReservationEvent(userId, queue.getBook().getTitle()));
      return true;
    }
    return false;
  }

  @Override
  @Transactional
  public boolean cancelQueueEntry(Long queueId, Long userId) {
    Optional<BookWaitingQueue> optEntry = bookWaitingQueueRepository.findById(queueId);

    if (optEntry.isEmpty()) {
      return false;
    }

    BookWaitingQueue entry = optEntry.get();

    // Verify ownership
    if (!entry.getUser().getId().equals(userId)) {
      return false;
    }

    // Only allow cancellation of waiting or notified status
    if (entry.getStatus() != QueueStatus.WAITING && entry.getStatus() != QueueStatus.NOTIFIED) {
      return false;
    }

    // Store book ID and notification status before deleting the entry
    Long bookId = entry.getBook().getId();
    boolean wasNotified = entry.getStatus() == QueueStatus.NOTIFIED;
    String bookTitle = entry.getBook().getTitle();

    // Delete the entry instead of just updating status
    bookWaitingQueueRepository.delete(entry);

    // If the entry was NOTIFIED, process next in queue
    if (wasNotified) {
      processNextInQueue(bookId);
    }

    eventPublisher.publishEvent(new BookQueueCancellationEvent(userId, bookTitle));

    return true;
  }


  @Override
  @Transactional(readOnly = true)
  public List<BookNotificationDTO> getUserActiveQueues(Long userId) {
    List<QueueStatus> activeStatuses = Arrays.asList(
            QueueStatus.WAITING,
            QueueStatus.NOTIFIED);

    List<BookWaitingQueue> notifications = bookWaitingQueueRepository
            .findByUserIdAndStatusInOrderByCreatedAtDesc(userId, activeStatuses);

    return notifications.stream()
            .map(notificationMapper::mapToBookNotificationDTO)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public int getUserQueuePosition(Long bookId, Long userId) {
    List<QueueStatus> activeStatuses = Arrays.asList(
            QueueStatus.WAITING,
            QueueStatus.NOTIFIED);

    List<BookWaitingQueue> queueEntries = bookWaitingQueueRepository
            .findByUserIdAndStatusInOrderByCreatedAtDesc(userId, activeStatuses);

    for (BookWaitingQueue entry : queueEntries) {
      if (entry.getBook().getId().equals(bookId)) {
        return entry.getPosition();
      }
    }

    return -1;
  }
}