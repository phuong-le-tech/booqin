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
import org.springframework.lang.NonNull;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
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
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public boolean addToWaitingQueue(@NonNull Long bookId, @NonNull Long userId) {
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

    // If book is already available, notify user immediately instead of adding to queue
    if (available.isPresent()) {
      eventPublisher.publishEvent(new BookAvailableEvent(userId, book.getTitle(), book.getId()));
      return true;
    }

    // Calculate position in queue
    int position = bookWaitingQueueRepository.findMaxPositionByBookId(bookId)
            .orElse(0) + 1;

    // Create and save queue entry
    BookWaitingQueue queueEntry = new BookWaitingQueue(book, user, position);
    bookWaitingQueueRepository.save(queueEntry);

    // Publish queue joined event
    eventPublisher.publishEvent(new BookQueueJoinedEvent(userId, book.getTitle(), position));
    return true;
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public void processNextInQueue(@NonNull Long bookId) {
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

      // Update queue entry status to NOTIFIED
      queueEntry.setStatus(QueueStatus.NOTIFIED);
      queueEntry.setNotifiedAt(LocalDateTime.now());
      queueEntry.setReservationExpiresAt(LocalDateTime.now().plusHours(1)); // 1-hour reservation window
      bookWaitingQueueRepository.save(queueEntry);

      // Notify user about book availability
      eventPublisher.publishEvent(new BookAvailableEvent(user.getId(), book.getTitle(), book.getId()));
    }
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public void processExpiredNotifications() {
    List<BookWaitingQueue> expiredNotifications = bookWaitingQueueRepository
            .findExpiredNotifications(LocalDateTime.now());

    for (BookWaitingQueue entry : expiredNotifications) {
      // Mark notification as expired
      entry.setStatus(QueueStatus.EXPIRED);
      bookWaitingQueueRepository.save(entry);
      Book book = entry.getBook();
      User user = entry.getUser();

      // Notify user about expiration
      eventPublisher.publishEvent(new BookQueueExpirationEvent(user.getId(), book.getTitle()));

      // Process next person in queue
      processNextInQueue(book.getId());
    }
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public boolean reserveBookFromQueue(@NonNull Long queueId, @NonNull Long userId) {
    Optional<BookWaitingQueue> optEntry = bookWaitingQueueRepository.findById(queueId);

    if (optEntry.isEmpty()) {
      return false; // Queue entry not found
    }

    BookWaitingQueue queue = optEntry.get();

    // Verify the queue entry belongs to the user
    if (!queue.getUser().getId().equals(userId)) {
      return false;
    }

    // Verify the queue entry is in NOTIFIED status
    if (queue.getStatus() != QueueStatus.NOTIFIED) {
      return false;
    }

    // Check if reservation window has expired
    if (queue.getReservationExpiresAt().isBefore(LocalDateTime.now())) {
      queue.setStatus(QueueStatus.EXPIRED);
      bookWaitingQueueRepository.save(queue);
      processNextInQueue(queue.getBook().getId());
      return false;
    }

    // Attempt to reserve the book
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
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public boolean cancelQueueEntry(@NonNull Long queueId, @NonNull Long userId) {
    Optional<BookWaitingQueue> optEntry = bookWaitingQueueRepository.findById(queueId);

    if (optEntry.isEmpty()) {
      return false; // Queue entry not found
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

    // Notify user of cancellation
    eventPublisher.publishEvent(new BookQueueCancellationEvent(userId, bookTitle));

    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public List<BookNotificationDTO> getUserActiveQueues(@NonNull Long userId) {
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
  public int getUserQueuePosition(@NonNull Long bookId, @NonNull Long userId) {
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

  /* Recovery methods for handling optimistic locking failures */

  @Recover
  public boolean recoverAddToWaitingQueue(ObjectOptimisticLockingFailureException ex, Long bookId, Long userId) {
    throw new ConcurrentModificationException("Concurrent modification detected when adding to waiting queue. Please try again.", ex);
  }

  @Recover
  public void recoverProcessNextInQueue(ObjectOptimisticLockingFailureException ex, Long bookId) {
    throw new ConcurrentModificationException("Concurrent modification detected when processing next in queue. Please try again.", ex);
  }

  @Recover
  public void recoverProcessExpiredNotifications(ObjectOptimisticLockingFailureException ex) {
    throw new ConcurrentModificationException("Concurrent modification detected when processing expired notifications. Please try again.", ex);
  }

  @Recover
  public boolean recoverReserveBookFromQueue(ObjectOptimisticLockingFailureException ex, Long queueId, Long userId) {
    throw new ConcurrentModificationException("Concurrent modification detected when reserving book from queue. Please try again.", ex);
  }

  @Recover
  public boolean recoverCancelQueueEntry(ObjectOptimisticLockingFailureException ex, Long queueId, Long userId) {
    throw new ConcurrentModificationException("Concurrent modification detected when canceling queue entry. Please try again.", ex);
  }
}