package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.lending.BookLendingShow;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.enums.LendStatus;
import fr.uge.booqIn.event.BooqInEventPublisher;
import fr.uge.booqIn.event.publishable.BookAvailableEvent;
import fr.uge.booqIn.event.publishable.BookNotAvailableEvent;
import fr.uge.booqIn.event.publishable.BookReturnedEvent;
import fr.uge.booqIn.mapper.BookLendingMapper;
import fr.uge.booqIn.service.PdfCreator;
import fr.uge.booqIn.repository.*;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;

import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookLendingServiceImpl implements BookLendingInterface {

  private final BookLendingRepository bookLendingRepository;
  private final UserServiceImpl userService;
  private final BookRepository bookRepository;
  private final BookLendingMapper bookLendingMapper;
  private final BookServiceImpl bookService;
  private final BooqInEventPublisher eventPublisher;
  private final UserRepository userRepository;
  private final PdfCreator pdfCreator = new PdfCreator();

  public BookLendingServiceImpl(
          BookLendingRepository bookLendingRepository,
          UserServiceImpl userService,
          BookRepository bookRepository,
          BookLendingMapper bookLendingMapper,
          BookServiceImpl bookService,
          BooqInEventPublisher eventPublisher,
          UserRepository userRepository
  ) {
    this.bookLendingRepository = bookLendingRepository;
    this.userService = userService;
    this.bookRepository = bookRepository;
    this.bookLendingMapper = bookLendingMapper;
    this.bookService = bookService;
    this.eventPublisher = eventPublisher;
    this.userRepository = userRepository;
  }

  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class,
                  ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public boolean confirmLoan(@NonNull Long lendingId) {
    Optional<BookLending> optionalLending = bookLendingRepository.findById(lendingId);

    if (optionalLending.isEmpty()) {
      return false;
    }

    BookLending lending = optionalLending.get();

    if (lending.getStatus() != BookStatus.SHIPPED) {
      return false;
    }

    lending.setStatus(BookStatus.BORROWED);
    lending.setStartDate(LocalDateTime.now());
    lending.setDueDate(lending.getStartDate().plusWeeks(4));

    bookLendingRepository.save(lending);

    return true;
  }


  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class,
                  ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public boolean returnBook(@NonNull Long lendingId) {
    Optional<BookLending> optionalLending = bookLendingRepository.findById(lendingId);

    if (optionalLending.isEmpty()) {
      return false;
    }

    BookLending lending = optionalLending.get();

    if (lending.getStatus() != BookStatus.BORROWED) {
      return false;
    }

    lending.setStatus(BookStatus.AVAILABLE);
    lending.setShippingTicketPDF(null);
    lending.setBorrower(null);
    lending.setReturnDate(null);

    bookLendingRepository.save(lending);
    eventPublisher.publishEvent(new BookReturnedEvent(lending.getBook().getId()));
    return true;
  }

  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public boolean reserveBook(@NonNull Long bookId, @NonNull Long borrowerId) {
    Optional<Book> optionalBook = bookRepository.findById(bookId);
    Optional<User> optionalUser = Optional.ofNullable(userService.findUserById(borrowerId));
    if (optionalBook.isEmpty() || optionalUser.isEmpty()) {
      return false;
    }
    Book book = optionalBook.get();
    User borrower = optionalUser.get();

    var bookLending = findBookLendingByBookId(bookId);
    if (bookLending.isEmpty()) {
      eventPublisher.publishEvent(new BookNotAvailableEvent(bookId, borrowerId));
      return false;
    }
    List<BookLending> availableLendings = bookLendingRepository.findAvailableLendingsForBookOrderByLenderPoints(bookId);
    BookLending selectedLending = availableLendings.getFirst();
    selectedLending.setBorrower(borrower);
    selectedLending.setReservedAt(LocalDateTime.now());
    selectedLending.setStatus(BookStatus.RESERVED);
    bookLendingRepository.save(selectedLending);
    User lender = selectedLending.getLender();

    return true;
  }

  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public boolean cancelBook(@NonNull Long bookId, @NonNull Long userId) {
    User user = userService.findUserById(userId);
    Optional<Book> optBook = bookRepository.findById(bookId);
    if (optBook.isEmpty()) {
      return false;
    }
    Optional<BookLending> optLendings = bookLendingRepository.findByBookAndBorrower(optBook.get(), user);
    if (optLendings.isPresent()) {
      BookLending bookLending = optLendings.get();
      bookLendingRepository.delete(bookLending);
      return true;
    }
    return false;
  }

  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public boolean markAsShipped(@NonNull Long lendingId) {
    Optional<BookLending> optionalLending = bookLendingRepository.findById(lendingId);

    if (optionalLending.isEmpty()) {
      return false;
    }

    BookLending lending = optionalLending.get();

    if (lending.getStatus() != BookStatus.PENDING_SHIPMENT) {
      return false;
    }
    lending.setStatus(BookStatus.SHIPPED);
    lending.setStartDate(LocalDateTime.now());
    lending.setReturnDate(lending.getStartDate().plusWeeks(2));
    bookLendingRepository.save(lending);
    return true;
  }

  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public void markAsPending(@NonNull Long bookId, @NonNull Long userId) {
    Optional<Book> optBook = bookRepository.findById(bookId);
    User user = userService.findUserById(userId);
    if (optBook.isEmpty()) {
      return;
    }
    Optional<BookLending> optLendings = bookLendingRepository.findByBookAndBorrower(optBook.get(), user);
    if (optLendings.isPresent()) {
      BookLending bookLending = optLendings.get();
      bookLending.setStatus(BookStatus.PENDING_SHIPMENT);
      var pdf = pdfCreator.generateShippingTicketPDF(bookLending);
      bookLending.setShippingTicketPDF(pdf);
      User lender = bookLending.getLender();
      if (lender != null) {
        lender.incrementLendingPoints();
        userRepository.save(lender);
      }
      bookLendingRepository.save(bookLending);
    }
  }

  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public boolean markAsReturn(@NonNull Long bookId, @NonNull Long userId) {
    Optional<Book> optBook = bookRepository.findById(bookId);
    User user = userService.findUserById(userId);
    if (optBook.isEmpty()) {
      return false;
    }
    Optional<BookLending> optLendings = bookLendingRepository.findByBookAndBorrower(optBook.get(), user);
    if (optLendings.isPresent()) {
      BookLending bookLending = optLendings.get();
      if (bookLending.getStatus() == BookStatus.BORROWED) {
        bookLending.setStatus(BookStatus.AVAILABLE);
        bookLendingRepository.save(bookLending);
        return true;
      } else {
        return false;
      }
    }
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public List<BookLending> findUserLendings(@NonNull Long userId) {
    User user = userService.findUserById(userId);
    return bookLendingRepository.findByBorrower(user);
  }

  @Override
  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public void ListClearBookstoLend(@NonNull List<BookLending> bookLendings) {
    for (BookLending bookLending : bookLendings) {
      bookLending.setBorrower(null);
      bookLending.setStatus(BookStatus.AVAILABLE);
      bookLendingRepository.save(bookLending);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<BookLendingShow> findBorrowedLendingsAsDTO(@NonNull Long userId) {
    User user = userService.findUserById(userId);
    List<BookLending> borrows = bookLendingRepository.findByBorrower(user);
    for (BookLending borrow : borrows) {
      initializeBookLendingLazyRelations(borrow);
    }
    return borrows.stream()
            .map(bookLendingMapper::toBookLendingShow)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<BookLendingShow> findLentBooksAsDTO(@NonNull Long userId) {
    User user = userService.findUserById(userId);
    List<BookLending> lendings = bookLendingRepository.findBookLendingsByLender(user);

    for (BookLending lending : lendings) {
      initializeBookLendingLazyRelations(lending);
    }

    return lendings.stream()
            .map(bookLendingMapper::toBookLendingShow)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public LendingResponseDTO findAllUserLendingsAsDTO(@NonNull Long userId, @NonNull LendStatus type) {
    boolean containsAll = type == LendStatus.ALL;

    List<BookLendingShow> borrowedBooks = containsAll || type == LendStatus.BORROWED
            ? findBorrowedLendingsAsDTO(userId)
            : List.of();

    List<BookLendingShow> lentBooks = containsAll || type == LendStatus.LENT
            ? findLentBooksAsDTO(userId)
            : List.of();

    return new LendingResponseDTO(borrowedBooks, lentBooks, containsAll);
  }

  @Override
  @Transactional
  public boolean loanable(@NonNull Long bookId, @NonNull Long userId) {
    Optional<Book> bookOpt = bookRepository.findById(bookId);
    if (bookOpt.isEmpty()) {
      return false;
    }
    Book book = bookOpt.get();

    User user = userService.findUserById(userId);
    if (user == null) {
      return false;
    }

    boolean alreadyLoanable = user.getLentBooks().stream()
            .anyMatch(toL -> toL.getBook().getId().equals(bookId));

    if (alreadyLoanable) {
      return false;
    }

    var booklend = new BookLending(book, user);
    bookLendingRepository.save(booklend);
    user.addLentBook(booklend);
    userRepository.save(user);

    eventPublisher.publishEvent(new BookAvailableEvent(user.getId(), book.getTitle(), book.getId()));
    return true;
  }

  @Override
  @Transactional
  public boolean isNoMoreLoanable(@NonNull Long bookId, @NonNull Long userId) {
    Optional<Book> bookOpt = bookRepository.findById(bookId);
    if (bookOpt.isEmpty()) {
      return false;
    }
    Book book = bookOpt.get();

    User user = userService.findUserById(userId);
    if (user == null) {
      return false;
    }
    Optional<BookLending> bookLending = userRepository.findById(userId).get().getLentBooks().stream()
            .filter(toL -> toL.getBook().getId().equals(bookId))
            .findFirst();

    if (bookLending.isEmpty()) {
      return false;
    }

    user.removeLentBook(bookLending.get());
    bookLendingRepository.delete(bookLending.get());
    userRepository.save(user);
    return true;
  }

  @Override
  @Transactional
  public byte[] getShippingTicketBytes(@NonNull Long lendingId) {
    Optional<BookLending> lendingOpt = bookLendingRepository.findById(lendingId);
    if (lendingOpt.isEmpty()) {
      return null;
    }

    BookLending lending = lendingOpt.get();
    return lending.getShippingTicketPDF();
  }

  @Override
  @Transactional
  public Optional<BookLending> findBookLendingByBookId(@NonNull Long bookId) {
    return Optional
            .ofNullable(bookLendingRepository.findByBookIdAndBorrowerIsNullAndStatus(bookId, BookStatus.AVAILABLE));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<BookLending> findByBookIdAndBorrowerIsNullAndStatus(@NonNull Long bookId) {
    return Optional.ofNullable(bookLendingRepository.findByBookIdAndBorrowerIsNullAndStatus(bookId, BookStatus.AVAILABLE));
  }

  private void initializeBookLendingLazyRelations(BookLending bookLending) {

    if (bookLending.getBook() != null) {
      Long bookId = bookLending.getBook().getId();

      Book completeBook = bookService.findBookById(bookId);

      bookLending.getBook().setTitle(completeBook.getTitle());
      bookLending.getBook().setDescription(completeBook.getDescription());
      bookLending.getBook().setPublicationDate(completeBook.getPublicationDate());
      bookLending.getBook().setAuthors(completeBook.getAuthors());
      bookLending.getBook().setCategories(completeBook.getCategories());
      bookLending.getBook().setIsbns(completeBook.getIsbns());
    }

  }

  @Recover
  public boolean recoverFromOptimisticLockingFailureOnConfirmLoan(ObjectOptimisticLockingFailureException ex,
                                                                  Long lendingId) {
    throw new ConcurrentModificationException("The book lending has been modified by someone else. Please try again.",
            ex);
  }

  @Recover
  public boolean recoverFromOptimisticLockingFailureOnReturnBook(ObjectOptimisticLockingFailureException ex,
                                                                 Long lendingId) {
    throw new ConcurrentModificationException("The book lending has been modified by someone else. Please try again.",
            ex);
  }

  @Recover
  public boolean recoverFromOptimisticLockingFailureOnReserveBook(ObjectOptimisticLockingFailureException ex,
                                                                  Long bookId, Long borrowerId) {
    throw new ConcurrentModificationException("The book lending has been modified by someone else. Please try again.",
            ex);
  }

  @Recover
  public boolean recoverFromOptimisticLockingFailureOnCancelBook(ObjectOptimisticLockingFailureException ex,
                                                                 Long bookId, Long userId) {
    throw new ConcurrentModificationException("The book lending has been modified by someone else. Please try again.",
            ex);
  }

  @Recover
  public boolean recoverFromOptimisticLockingFailureOnMarkAsShipped(ObjectOptimisticLockingFailureException ex,
                                                                    Long lendingId) {
    throw new ConcurrentModificationException("The book lending has been modified by someone else. Please try again.",
            ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureOnMarkAsPending(ObjectOptimisticLockingFailureException ex,
                                                                 Long bookId, Long userId) {
    throw new ConcurrentModificationException("The book lending has been modified by someone else. Please try again.",
            ex);
  }

  @Recover
  public boolean recoverFromOptimisticLockingFailureOnMarkAsReturn(ObjectOptimisticLockingFailureException ex,
                                                                   Long bookId, Long userId) {
    throw new ConcurrentModificationException("The book lending has been modified by someone else. Please try again.",
            ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureOnListClear(ObjectOptimisticLockingFailureException ex,
                                                             List<BookLending> bookLendings) {
    throw new ConcurrentModificationException(
            "The book lending list has been modified by someone else. Please try again.", ex);
  }

}
