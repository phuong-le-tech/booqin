package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.lending.BookLendingShow;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.event.BooqInEventPublisher;
import fr.uge.booqIn.event.publishable.BookReturnedEvent;
import fr.uge.booqIn.mapper.BookLendingMapper;
import fr.uge.booqIn.pdf.PdfCreator;
import fr.uge.booqIn.repository.BookLendingRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.impl.BookLendingServiceImpl;
import fr.uge.booqIn.service.impl.BookServiceImpl;
import fr.uge.booqIn.service.impl.UserServiceImpl;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("BookLendingService Tests")
class BookLendingServiceTest {

  private BookLendingRepository bookLendingRepository;
  private UserServiceImpl userService;
  private BookRepository bookRepository;
  private BookLendingMapper bookLendingMapper;
  private BookServiceImpl bookService;
  private BooqInEventPublisher eventPublisher;
  private UserRepository userRepository;
  private PdfCreator pdfCreator;
  private BookLendingInterface bookLendingService;
  private User testUser;
  private User testLender;
  private Book testBook;
  private BookLending testLending;

  @BeforeEach
  void setUp() {
    bookLendingRepository = Mockito.mock(BookLendingRepository.class);
    userService = Mockito.mock(UserServiceImpl.class);
    bookRepository = Mockito.mock(BookRepository.class);
    bookLendingMapper = Mockito.mock(BookLendingMapper.class);
    bookService = Mockito.mock(BookServiceImpl.class);
    eventPublisher = Mockito.mock(BooqInEventPublisher.class);
    userRepository = Mockito.mock(UserRepository.class);
    pdfCreator = Mockito.mock(PdfCreator.class);

    bookLendingService = new BookLendingServiceImpl(
            bookLendingRepository,
            userService,
            bookRepository,
            bookLendingMapper,
            bookService,
            eventPublisher,
            userRepository
    );

    // Setup test data
    testUser = new User();
    testUser.setId(1L);
    testUser.setFirstName("John");
    testUser.setLastName("Doe");

    testLender = new User();
    testLender.setId(2L);
    testLender.setFirstName("Jane");
    testLender.setLastName("Smith");
    testLender.setLendingPoints(10);

    testBook = new Book();
    testBook.setId(1L);
    testBook.setTitle("Test Book");

    testLending = new BookLending();
    testLending.setId(1L);
    testLending.setBook(testBook);
    testLending.setBorrower(testUser);
    testLending.setLender(testLender);
    testLending.setStatus(BookStatus.SHIPPED);
    testLending.setReservedAt(LocalDateTime.now().minusDays(1));
  }

  @Test
  @DisplayName("confirmLoan - Should confirm loan and update status to BORROWED")
  void confirmLoan_WithValidLendingId_ShouldConfirmLoan() {
    // Given
    Long lendingId = testLending.getId();

    when(bookLendingRepository.findById(eq(lendingId))).thenReturn(Optional.empty());

    // When
    boolean result = bookLendingService.confirmLoan(lendingId);

    // Then
    assertFalse(result);

    verify(bookLendingRepository).findById(eq(lendingId));
    verifyNoMoreInteractions(bookLendingRepository);
  }

  @Test
  @DisplayName("confirmLoan - Should return false when status is not SHIPPED")
  void confirmLoan_WithInvalidStatus_ShouldReturnFalse() {
    // Given
    Long lendingId = testLending.getId();
    testLending.setStatus(BookStatus.RESERVED); // Invalid status for confirming loan

    when(bookLendingRepository.findById(eq(lendingId))).thenReturn(Optional.of(testLending));

    // When
    boolean result = bookLendingService.confirmLoan(lendingId);

    // Then
    assertFalse(result);

    verify(bookLendingRepository).findById(eq(lendingId));
    verifyNoMoreInteractions(bookLendingRepository);
  }

  @Test
  @DisplayName("returnBook - Should update status to AVAILABLE")
  void returnBook_WithValidLendingId_ShouldReturnBook() {
    // Given
    Long lendingId = testLending.getId();
    testLending.setStatus(BookStatus.BORROWED); // Valid status for returning book

    when(bookLendingRepository.findById(eq(lendingId))).thenReturn(Optional.of(testLending));
    when(bookLendingRepository.save(any(BookLending.class))).thenReturn(testLending);
    doNothing().when(eventPublisher).publishEvent(any(BookReturnedEvent.class));

    // When
    boolean result = bookLendingService.returnBook(lendingId);

    // Then
    assertTrue(result);
    assertEquals(BookStatus.AVAILABLE, testLending.getStatus());
    assertNull(testLending.getReturnDate());

    verify(bookLendingRepository).findById(eq(lendingId));
    verify(bookLendingRepository).save(eq(testLending));
    verify(eventPublisher).publishEvent(any(BookReturnedEvent.class));
  }

  @Test
  @DisplayName("returnBook - Should return false when lending not found")
  void returnBook_WithNonExistingLendingId_ShouldReturnFalse() {
    // Given
    Long lendingId = 999L;

    when(bookLendingRepository.findById(eq(lendingId))).thenReturn(Optional.empty());

    // When
    boolean result = bookLendingService.returnBook(lendingId);

    // Then
    assertFalse(result);

    verify(bookLendingRepository).findById(eq(lendingId));
    verifyNoMoreInteractions(bookLendingRepository);
    verifyNoInteractions(eventPublisher);
  }

  @Test
  @DisplayName("returnBook - Should return false when status is not BORROWED")
  void returnBook_WithInvalidStatus_ShouldReturnFalse() {
    // Given
    Long lendingId = testLending.getId();
    testLending.setStatus(BookStatus.SHIPPED); // Invalid status for returning book

    when(bookLendingRepository.findById(eq(lendingId))).thenReturn(Optional.of(testLending));

    // When
    boolean result = bookLendingService.returnBook(lendingId);

    // Then
    assertFalse(result);

    verify(bookLendingRepository).findById(eq(lendingId));
    verifyNoMoreInteractions(bookLendingRepository);
    verifyNoInteractions(eventPublisher);
  }

  @Test
  @DisplayName("cancelBook - Should cancel book reservation")
  void cancelBook_WithValidBookAndUser_ShouldCancelReservation() {
    // Given
    Long bookId = testBook.getId();
    Long userId = testUser.getId();

    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.of(testBook));
    when(bookLendingRepository.findByBookAndBorrower(eq(testBook), eq(testUser))).thenReturn(Optional.of(testLending));
    doNothing().when(bookLendingRepository).delete(any(BookLending.class));

    // When
    boolean result = bookLendingService.cancelBook(bookId, userId);

    // Then
    assertTrue(result);

    verify(userService).findUserById(eq(userId));
    verify(bookRepository).findById(eq(bookId));
    verify(bookLendingRepository).findByBookAndBorrower(eq(testBook), eq(testUser));
    verify(bookLendingRepository).delete(eq(testLending));
  }

  @Test
  @DisplayName("markAsShipped - Should mark lending as shipped")
  void markAsShipped_WithValidLendingId_ShouldMarkAsShipped() {
    // Given
    Long lendingId = testLending.getId();
    testLending.setStatus(BookStatus.PENDING_SHIPMENT); // Valid status for marking as shipped

    when(bookLendingRepository.findById(eq(lendingId))).thenReturn(Optional.of(testLending));
    when(bookLendingRepository.save(any(BookLending.class))).thenReturn(testLending);

    // When
    boolean result = bookLendingService.markAsShipped(lendingId);

    // Then
    assertTrue(result);
    assertEquals(BookStatus.SHIPPED, testLending.getStatus());
    assertNotNull(testLending.getStartDate());
    assertNotNull(testLending.getReturnDate());

    verify(bookLendingRepository).findById(eq(lendingId));
    verify(bookLendingRepository).save(eq(testLending));
  }

  @Test
  @DisplayName("markAsPending - Should mark lending as pending shipment")
  void markAsPending_WithValidBookAndUser_ShouldMarkAsPending() {
    // Given
    Long bookId = testBook.getId();
    Long userId = testUser.getId();

    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.of(testBook));
    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(bookLendingRepository.findByBookAndBorrower(eq(testBook), eq(testUser))).thenReturn(Optional.of(testLending));
    when(bookLendingRepository.save(any(BookLending.class))).thenReturn(testLending);
    when(userRepository.save(any(User.class))).thenReturn(testLender);

    // When
    bookLendingService.markAsPending(bookId, userId);

    // Then
    assertEquals(BookStatus.PENDING_SHIPMENT, testLending.getStatus());

    verify(bookRepository).findById(eq(bookId));
    verify(userService).findUserById(eq(userId));
    verify(bookLendingRepository).findByBookAndBorrower(eq(testBook), eq(testUser));
    verify(bookLendingRepository).save(eq(testLending));

    // Check if lender points were incremented
    verify(userRepository).save(eq(testLender));
  }

  @Test
  @DisplayName("markAsReturn - Should mark lending as returned")
  void markAsReturn_WithValidBookAndUser_ShouldMarkAsReturned() {
    // Given
    Long bookId = testBook.getId();
    Long userId = testUser.getId();
    testLending.setStatus(BookStatus.BORROWED); // Valid status for marking as returned

    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.of(testBook));
    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(bookLendingRepository.findByBookAndBorrower(eq(testBook), eq(testUser))).thenReturn(Optional.of(testLending));
    when(bookLendingRepository.save(any(BookLending.class))).thenReturn(testLending);

    // When
    boolean result = bookLendingService.markAsReturn(bookId, userId);

    // Then
    assertTrue(result);
    assertEquals(BookStatus.AVAILABLE, testLending.getStatus());

    verify(bookRepository).findById(eq(bookId));
    verify(userService).findUserById(eq(userId));
    verify(bookLendingRepository).findByBookAndBorrower(eq(testBook), eq(testUser));
    verify(bookLendingRepository).save(eq(testLending));
  }

  @Test
  @DisplayName("findUserLendings - Should return list of user lendings")
  void findUserLendings_ShouldReturnUserLendings() {
    // Given
    Long userId = testUser.getId();
    List<BookLending> userLendings = Collections.singletonList(testLending);

    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(bookLendingRepository.findByBorrower(eq(testUser))).thenReturn(userLendings);

    // When
    List<BookLending> result = bookLendingService.findUserLendings(userId);

    // Then
    assertEquals(userLendings, result);

    verify(userService).findUserById(eq(userId));
    verify(bookLendingRepository).findByBorrower(eq(testUser));
  }

  @Test
  @DisplayName("ListClearBookstoLend - Should clear books to lend")
  void ListClearBookstoLend_ShouldClearBooksToLend() {
    // Given
    List<BookLending> lendings = Arrays.asList(testLending);
    when(bookLendingRepository.save(any(BookLending.class))).thenReturn(testLending);

    // When
    bookLendingService.ListClearBookstoLend(lendings);

    // Then
    assertNull(testLending.getBorrower());
    assertEquals(BookStatus.AVAILABLE, testLending.getStatus());

    verify(bookLendingRepository).save(eq(testLending));
  }

  @Test
  @DisplayName("loanable - Should make book loanable by user")
  void loanable_WithValidBookAndUser_ShouldMakeBookLoanable() {
    // Given
    Long bookId = testBook.getId();
    Long userId = testUser.getId();

    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.of(testBook));
    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(bookLendingRepository.save(any(BookLending.class))).thenReturn(testLending);
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    boolean result = bookLendingService.loanable(bookId, userId);

    // Then
    assertTrue(result);

    verify(bookRepository).findById(eq(bookId));
    verify(userService).findUserById(eq(userId));
    verify(bookLendingRepository).save(any(BookLending.class));
    verify(userRepository).save(any(User.class));

    ArgumentCaptor<BookLending> lendingCaptor = ArgumentCaptor.forClass(BookLending.class);
    verify(bookLendingRepository).save(lendingCaptor.capture());
    BookLending capturedLending = lendingCaptor.getValue();
    assertEquals(testBook, capturedLending.getBook());
    assertEquals(testUser, capturedLending.getLender());
  }

  @Test
  @DisplayName("isNoMoreLoanable - Should make book no more loanable by user")
  void isNoMoreLoanable_WithValidBookAndUser_ShouldMakeBookNoMoreLoanable() {
    // Given
    Long bookId = testBook.getId();
    Long userId = testUser.getId();

    testUser.addLentBook(testLending);

    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.of(testBook));
    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    doNothing().when(bookLendingRepository).delete(any(BookLending.class));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    boolean result = bookLendingService.isNoMoreLoanable(bookId, userId);

    // Then
    assertTrue(result);

    verify(bookRepository).findById(eq(bookId));
    verify(userService).findUserById(eq(userId));
    verify(userRepository).findById(eq(userId));
    verify(bookLendingRepository).delete(any(BookLending.class));
    verify(userRepository).save(any(User.class));
  }
}