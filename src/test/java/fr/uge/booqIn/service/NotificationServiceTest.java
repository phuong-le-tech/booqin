package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.notification.BookNotificationDTO;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookWaitingQueue;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.QueueStatus;
import fr.uge.booqIn.event.BooqInEventPublisher;
import fr.uge.booqIn.event.publishable.BookNotAvailableEvent;
import fr.uge.booqIn.event.publishable.BookReturnedEvent;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.NotificationMapper;
import fr.uge.booqIn.repository.BookWaitingQueueRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.impl.BookWaitingQueueServiceImpl;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.BookWaitingQueueServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("NotificationService Tests")
class NotificationServiceTest {

  private BookWaitingQueueRepository notificationQueueRepository;
  private BookRepository bookRepository;
  private UserRepository userRepository;
  private BookLendingInterface bookLendingService;
  private NotificationMapper notificationMappeur;
  private BookWaitingQueueServiceInterface notificationService;
  private User testUser;
  private Book testBook;
  private BookWaitingQueue testNotification;
  private BooqInEventPublisher  booqInEventPublisher;

  @BeforeEach
  void setUp() {
    notificationQueueRepository = Mockito.mock(BookWaitingQueueRepository.class);
    bookRepository = Mockito.mock(BookRepository.class);
    userRepository = Mockito.mock(UserRepository.class);
    bookLendingService = Mockito.mock(BookLendingInterface.class);
    notificationMappeur = Mockito.mock(NotificationMapper.class);
    booqInEventPublisher = Mockito.mock(BooqInEventPublisher.class);

    notificationService = new BookWaitingQueueServiceImpl(
            notificationQueueRepository,
            bookRepository,
            userRepository,
            bookLendingService,
            notificationMappeur,
        booqInEventPublisher
    );

    // Setup test data
    testUser = new User();
    testUser.setId(1L);
    testUser.setFirstName("John");
    testUser.setLastName("Doe");

    testBook = new Book();
    testBook.setId(1L);
    testBook.setTitle("Test Book");

    testNotification = new BookWaitingQueue(testBook, testUser, 1);
    testNotification.setId(1L);
    testNotification.setStatus(QueueStatus.WAITING);
    testNotification.setCreatedAt(LocalDateTime.now());
  }

  @Test
  @DisplayName("addToNotificationQueue - Should add user to notification queue")
  void addToNotificationQueue_WithValidData_ShouldAddToQueue() {
    // Given
    Long bookId = testBook.getId();
    Long userId = testUser.getId();
    List<QueueStatus> activeStatuses = Arrays.asList(QueueStatus.WAITING, QueueStatus.NOTIFIED);

    when(notificationQueueRepository.existsByBookIdAndUserIdAndStatusIn(eq(bookId), eq(userId), eq(activeStatuses))).thenReturn(false);
    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.of(testBook));
    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(notificationQueueRepository.findMaxPositionByBookId(eq(bookId))).thenReturn(Optional.of(0));
    when(notificationQueueRepository.save(any(BookWaitingQueue.class))).thenReturn(testNotification);

    // When
    boolean result = notificationService.addToWaitingQueue(bookId, userId);

    // Then
    assertTrue(result);

    verify(notificationQueueRepository).existsByBookIdAndUserIdAndStatusIn(eq(bookId), eq(userId), eq(activeStatuses));
    verify(bookRepository).findById(eq(bookId));
    verify(userRepository).findById(eq(userId));
    verify(notificationQueueRepository).findMaxPositionByBookId(eq(bookId));
    verify(notificationQueueRepository).save(any(BookWaitingQueue.class));

    ArgumentCaptor<BookWaitingQueue> notificationCaptor = ArgumentCaptor.forClass(BookWaitingQueue.class);
    verify(notificationQueueRepository).save(notificationCaptor.capture());
    BookWaitingQueue capturedNotification = notificationCaptor.getValue();
    assertEquals(testBook, capturedNotification.getBook());
    assertEquals(testUser, capturedNotification.getUser());
    assertEquals(1, capturedNotification.getPosition());
    assertEquals(QueueStatus.WAITING, capturedNotification.getStatus());
  }

  @Test
  @DisplayName("addToNotificationQueue - Should return false when user already in queue")
  void addToNotificationQueue_WhenAlreadyInQueue_ShouldReturnFalse() {
    // Given
    Long bookId = testBook.getId();
    Long userId = testUser.getId();
    List<QueueStatus> activeStatuses = Arrays.asList(QueueStatus.WAITING, QueueStatus.NOTIFIED);

    when(notificationQueueRepository.existsByBookIdAndUserIdAndStatusIn(eq(bookId), eq(userId), eq(activeStatuses))).thenReturn(true);

    // When
    boolean result = notificationService.addToWaitingQueue(bookId, userId);

    // Then
    assertFalse(result);

    verify(notificationQueueRepository).existsByBookIdAndUserIdAndStatusIn(eq(bookId), eq(userId), eq(activeStatuses));
    verifyNoMoreInteractions(notificationQueueRepository);
    verifyNoInteractions(bookRepository);
    verifyNoInteractions(userRepository);
  }

  @Test
  @DisplayName("addToNotificationQueue - Should throw ResourceNotFoundException when book not found")
  void addToNotificationQueue_WithInvalidBookId_ShouldThrowException() {
    // Given
    Long bookId = 999L;
    Long userId = testUser.getId();
    List<QueueStatus> activeStatuses = Arrays.asList(QueueStatus.WAITING, QueueStatus.NOTIFIED);

    when(notificationQueueRepository.existsByBookIdAndUserIdAndStatusIn(eq(bookId), eq(userId), eq(activeStatuses))).thenReturn(false);
    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            notificationService.addToWaitingQueue(bookId, userId)
    );

    verify(notificationQueueRepository).existsByBookIdAndUserIdAndStatusIn(eq(bookId), eq(userId), eq(activeStatuses));
    verify(bookRepository).findById(eq(bookId));
    verifyNoInteractions(userRepository);
  }

  @Test
  @DisplayName("processNextInQueue - Should notify next user in queue")
  void processNextInQueue_WithValidData_ShouldNotifyNextUser() {
    // Given
    Long bookId = testBook.getId();

    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookLendingService.findBookLendingByBookId(eq(bookId))).thenReturn(Optional.empty());
    when(notificationQueueRepository.findFirstByBookIdAndStatusOrderByPositionAsc(eq(bookId), eq(QueueStatus.WAITING))).thenReturn(Optional.of(testNotification));
    when(notificationQueueRepository.save(any(BookWaitingQueue.class))).thenReturn(testNotification);

    // When
    notificationService.processNextInQueue(bookId);

    // Then
    verify(bookRepository).findById(eq(bookId));
    verify(bookLendingService).findBookLendingByBookId(eq(bookId));
    verify(notificationQueueRepository).findFirstByBookIdAndStatusOrderByPositionAsc(eq(bookId), eq(QueueStatus.WAITING));
    verify(notificationQueueRepository).save(any(BookWaitingQueue.class));

    ArgumentCaptor<BookWaitingQueue> notificationCaptor = ArgumentCaptor.forClass(BookWaitingQueue.class);
    verify(notificationQueueRepository).save(notificationCaptor.capture());
    BookWaitingQueue capturedNotification = notificationCaptor.getValue();
    assertEquals(QueueStatus.NOTIFIED, capturedNotification.getStatus());
    assertNotNull(capturedNotification.getNotifiedAt());
    assertNotNull(capturedNotification.getReservationExpiresAt());
  }

  @Test
  @DisplayName("processNextInQueue - Should not process when no waiting notifications")
  void processNextInQueue_WhenNoWaitingNotifications_ShouldNotProcess() {
    // Given
    Long bookId = testBook.getId();

    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookLendingService.findBookLendingByBookId(eq(bookId))).thenReturn(Optional.empty());
    when(notificationQueueRepository.findFirstByBookIdAndStatusOrderByPositionAsc(eq(bookId), eq(QueueStatus.WAITING))).thenReturn(Optional.empty());

    // When
    notificationService.processNextInQueue(bookId);

    // Then
    verify(bookRepository).findById(eq(bookId));
    verify(bookLendingService).findBookLendingByBookId(eq(bookId));
    verify(notificationQueueRepository).findFirstByBookIdAndStatusOrderByPositionAsc(eq(bookId), eq(QueueStatus.WAITING));
    verifyNoMoreInteractions(notificationQueueRepository);
  }

  @Test
  @DisplayName("processExpiredNotifications - Should process expired notifications")
  void processExpiredNotifications_ShouldUpdateExpiredNotifications() {
    // Given
    LocalDateTime now = LocalDateTime.now();

    BookWaitingQueue expiredNotification = new BookWaitingQueue(testBook, testUser, 1);
    expiredNotification.setId(2L);
    expiredNotification.setStatus(QueueStatus.NOTIFIED);
    expiredNotification.setCreatedAt(now.minusDays(1));
    expiredNotification.setNotifiedAt(now.minusDays(1));
    expiredNotification.setReservationExpiresAt(now.minusHours(1));

    List<BookWaitingQueue> expiredNotifications = Collections.singletonList(expiredNotification);

    when(notificationQueueRepository.findExpiredNotifications(any(LocalDateTime.class))).thenReturn(expiredNotifications);
    when(notificationQueueRepository.save(any(BookWaitingQueue.class))).thenReturn(expiredNotification);
    when(bookRepository.findById(eq(testBook.getId()))).thenReturn(Optional.of(testBook));
    when(bookLendingService.findBookLendingByBookId(eq(testBook.getId()))).thenReturn(Optional.empty());
    when(notificationQueueRepository.findFirstByBookIdAndStatusOrderByPositionAsc(eq(testBook.getId()), eq(QueueStatus.WAITING))).thenReturn(Optional.empty());

    // When
    notificationService.processExpiredNotifications();

    // Then
    verify(notificationQueueRepository).findExpiredNotifications(any(LocalDateTime.class));
    verify(notificationQueueRepository).save(eq(expiredNotification));
    verify(bookRepository).findById(eq(testBook.getId()));
    verify(bookLendingService).findBookLendingByBookId(eq(testBook.getId()));
    verify(notificationQueueRepository).findFirstByBookIdAndStatusOrderByPositionAsc(eq(testBook.getId()), eq(QueueStatus.WAITING));

    assertEquals(QueueStatus.EXPIRED, expiredNotification.getStatus());
  }

  @Test
  @DisplayName("reserveNotifiedBook - Should reserve book for notified user")
  void reserveNotifiedBook_WithValidData_ShouldReserveBook() {
    // Given
    Long notificationId = testNotification.getId();
    Long userId = testUser.getId();

    testNotification.setStatus(QueueStatus.NOTIFIED);
    testNotification.setNotifiedAt(LocalDateTime.now().minusMinutes(30));
    testNotification.setReservationExpiresAt(LocalDateTime.now().plusMinutes(30));

    when(notificationQueueRepository.findById(eq(notificationId))).thenReturn(Optional.of(testNotification));
    when(bookLendingService.reserveBook(eq(testBook.getId()), eq(userId))).thenReturn(true);
    when(notificationQueueRepository.save(any(BookWaitingQueue.class))).thenReturn(testNotification);

    // When
    boolean result = notificationService.reserveNotifiedBook(notificationId, userId);

    // Then
    assertTrue(result);

    verify(notificationQueueRepository).findById(eq(notificationId));
    verify(bookLendingService).reserveBook(eq(testBook.getId()), eq(userId));
    verify(notificationQueueRepository).save(any(BookWaitingQueue.class));

    ArgumentCaptor<BookWaitingQueue> notificationCaptor = ArgumentCaptor.forClass(BookWaitingQueue.class);
    verify(notificationQueueRepository).save(notificationCaptor.capture());
    BookWaitingQueue capturedNotification = notificationCaptor.getValue();
    assertEquals(QueueStatus.RESERVED, capturedNotification.getStatus());
  }

  @Test
  @DisplayName("reserveNotifiedBook - Should return false when notification not found")
  void reserveNotifiedBook_WithInvalidNotificationId_ShouldReturnFalse() {
    // Given
    Long notificationId = 999L;
    Long userId = testUser.getId();

    when(notificationQueueRepository.findById(eq(notificationId))).thenReturn(Optional.empty());

    // When
    boolean result = notificationService.reserveNotifiedBook(notificationId, userId);

    // Then
    assertFalse(result);

    verify(notificationQueueRepository).findById(eq(notificationId));
    verifyNoMoreInteractions(notificationQueueRepository);
    verifyNoInteractions(bookLendingService);
  }

  @Test
  @DisplayName("reserveNotifiedBook - Should return false when user does not match")
  void reserveNotifiedBook_WithWrongUser_ShouldReturnFalse() {
    // Given
    Long notificationId = testNotification.getId();
    Long wrongUserId = 999L;

    when(notificationQueueRepository.findById(eq(notificationId))).thenReturn(Optional.of(testNotification));

    // When
    boolean result = notificationService.reserveNotifiedBook(notificationId, wrongUserId);

    // Then
    assertFalse(result);

    verify(notificationQueueRepository).findById(eq(notificationId));
    verifyNoMoreInteractions(notificationQueueRepository);
    verifyNoInteractions(bookLendingService);
  }

  @Test
  @DisplayName("reserveNotifiedBook - Should return false when notification not in NOTIFIED status")
  void reserveNotifiedBook_WithWrongStatus_ShouldReturnFalse() {
    // Given
    Long notificationId = testNotification.getId();
    Long userId = testUser.getId();

    testNotification.setStatus(QueueStatus.WAITING); // Wrong status

    when(notificationQueueRepository.findById(eq(notificationId))).thenReturn(Optional.of(testNotification));

    // When
    boolean result = notificationService.reserveNotifiedBook(notificationId, userId);

    // Then
    assertFalse(result);

    verify(notificationQueueRepository).findById(eq(notificationId));
    verifyNoMoreInteractions(notificationQueueRepository);
    verifyNoInteractions(bookLendingService);
  }

  @Test
  @DisplayName("reserveNotifiedBook - Should mark as expired and process next when reservation expired")
  void reserveNotifiedBook_WhenReservationExpired_ShouldExpireAndProcessNext() {
    // Given
    Long notificationId = testNotification.getId();
    Long userId = testUser.getId();

    testNotification.setStatus(QueueStatus.NOTIFIED);
    testNotification.setNotifiedAt(LocalDateTime.now().minusHours(2));
    testNotification.setReservationExpiresAt(LocalDateTime.now().minusMinutes(30)); // Expired

    when(notificationQueueRepository.findById(eq(notificationId))).thenReturn(Optional.of(testNotification));
    when(notificationQueueRepository.save(any(BookWaitingQueue.class))).thenReturn(testNotification);
    when(bookRepository.findById(eq(testBook.getId()))).thenReturn(Optional.of(testBook));
    when(bookLendingService.findBookLendingByBookId(eq(testBook.getId()))).thenReturn(Optional.empty());
    when(notificationQueueRepository.findFirstByBookIdAndStatusOrderByPositionAsc(eq(testBook.getId()), eq(QueueStatus.WAITING))).thenReturn(Optional.empty());

    // When
    boolean result = notificationService.reserveNotifiedBook(notificationId, userId);

    // Then
    assertFalse(result);

    verify(notificationQueueRepository).findById(eq(notificationId));
    verify(notificationQueueRepository).save(any(BookWaitingQueue.class));
    verify(bookRepository).findById(eq(testBook.getId()));
    verify(bookLendingService).findBookLendingByBookId(eq(testBook.getId()));
    verify(notificationQueueRepository).findFirstByBookIdAndStatusOrderByPositionAsc(eq(testBook.getId()), eq(QueueStatus.WAITING));

    assertEquals(QueueStatus.EXPIRED, testNotification.getStatus());
  }

  @Test
  @DisplayName("getActiveNotifications - Should return active notifications for user")
  void getActiveNotifications_ShouldReturnActiveNotifications() {
    // Given
    Long userId = testUser.getId();
    List<QueueStatus> activeStatuses = Arrays.asList(QueueStatus.WAITING, QueueStatus.NOTIFIED);
    List<BookWaitingQueue> notifications = Collections.singletonList(testNotification);

    BookNotificationDTO notificationDTO = BookNotificationDTO.builder()
            .id(testNotification.getId())
            .bookId(testBook.getId())
            .bookTitle(testBook.getTitle())
            .status(testNotification.getStatus())
            .position(testNotification.getPosition())
            .createdAt(testNotification.getCreatedAt())
            .build();

    when(notificationQueueRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(eq(userId), eq(activeStatuses))).thenReturn(notifications);
    when(notificationMappeur.mapToBookNotificationDTO(any(BookWaitingQueue.class))).thenReturn(notificationDTO);

    // When
    List<BookNotificationDTO> result = notificationService.getActiveNotifications(userId);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(notificationDTO, result.get(0));

    verify(notificationQueueRepository).findByUserIdAndStatusInOrderByCreatedAtDesc(eq(userId), eq(activeStatuses));
    verify(notificationMappeur).mapToBookNotificationDTO(eq(testNotification));
  }

  @Test
  @DisplayName("handleBookReturnedEvent - Should process queue when book returned")
  void handleBookReturnedEvent_ShouldProcessQueue() {
    // Given
    Long bookId = testBook.getId();
    BookReturnedEvent event = new BookReturnedEvent(bookId);

    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookLendingService.findBookLendingByBookId(eq(bookId))).thenReturn(Optional.empty());
    when(notificationQueueRepository.findFirstByBookIdAndStatusOrderByPositionAsc(eq(bookId), eq(QueueStatus.WAITING))).thenReturn(Optional.empty());

    // When
    ((BookWaitingQueueServiceImpl) notificationService).handleBookReturnedEvent(event);

    // Then
    verify(bookRepository).findById(eq(bookId));
    verify(bookLendingService).findBookLendingByBookId(eq(bookId));
    verify(notificationQueueRepository).findFirstByBookIdAndStatusOrderByPositionAsc(eq(bookId), eq(QueueStatus.WAITING));
  }

  @Test
  @DisplayName("handleBookNotAvailableEvent - Should add to notification queue")
  void handleBookNotAvailableEvent_ShouldAddToQueue() {
    // Given
    Long bookId = testBook.getId();
    Long userId = testUser.getId();
    BookNotAvailableEvent event = new BookNotAvailableEvent(bookId, userId);
    List<QueueStatus> activeStatuses = Arrays.asList(QueueStatus.WAITING, QueueStatus.NOTIFIED);

    when(notificationQueueRepository.existsByBookIdAndUserIdAndStatusIn(eq(bookId), eq(userId), eq(activeStatuses))).thenReturn(false);
    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.of(testBook));
    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(notificationQueueRepository.findMaxPositionByBookId(eq(bookId))).thenReturn(Optional.of(0));
    when(notificationQueueRepository.save(any(BookWaitingQueue.class))).thenReturn(testNotification);

    // When
    ((BookWaitingQueueServiceImpl) notificationService).handleBookNotAvailableEvent(event);

    // Then
    verify(notificationQueueRepository).existsByBookIdAndUserIdAndStatusIn(eq(bookId), eq(userId), eq(activeStatuses));
    verify(bookRepository).findById(eq(bookId));
    verify(userRepository).findById(eq(userId));
    verify(notificationQueueRepository).findMaxPositionByBookId(eq(bookId));
    verify(notificationQueueRepository).save(any(BookWaitingQueue.class));
  }
}
