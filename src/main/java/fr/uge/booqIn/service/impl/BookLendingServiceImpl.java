package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.lending.BookLendingShow;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.mapper.BookLendingMapper;
import fr.uge.booqIn.repository.BookLendingRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.EventPublisherService;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import java.time.LocalDateTime;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookLendingServiceImpl implements BookLendingInterface {

  private final BookLendingRepository bookLendingRepository;
  private final UserServiceImpl userService;
  private final BookRepository bookRepository;
  private final BookLendingMapper bookLendingMapper;
  private final BookServiceImpl bookService;
  private final EventPublisherService eventPublisher;
  private final UserRepository userRepository;

  public BookLendingServiceImpl(
          BookLendingRepository bookLendingRepository,
          UserServiceImpl userService,
          BookRepository bookRepository,
          BookLendingMapper bookLendingMapper,
          BookServiceImpl bookService,
          EventPublisherService eventPublisher,
          UserRepository userRepository) {
    this.bookLendingRepository = bookLendingRepository;
    this.userService = userService;
    this.bookRepository = bookRepository;
    this.bookLendingMapper = bookLendingMapper;
    this.bookService = bookService;
    this.eventPublisher = eventPublisher;
    this.userRepository = userRepository;
  }

  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}", retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class}, backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public boolean confirmLoan(Long lendingId) {
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
    lending.setDueDate(lending.getStartDate().plusWeeks(2));

    bookLendingRepository.save(lending);

    return true;
  }

  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}", retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class}, backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public boolean returnBook(Long lendingId) {
    Optional<BookLending> optionalLending = bookLendingRepository.findById(lendingId);

    if (optionalLending.isEmpty()) {
      return false;
    }

    BookLending lending = optionalLending.get();

    if (lending.getStatus() != BookStatus.BORROWED) {
      return false;
    }

    lending.setStatus(BookStatus.AVAILABLE);
    lending.setReturnDate(null);

    bookLendingRepository.save(lending);
    eventPublisher.publishBookReturnedEvent(this, lending.getBook().getId());
    return true;
  }

  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}", retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class}, backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public boolean reserveBook(Long bookId, Long borrowerId) {
    Optional<Book> optionalBook = bookRepository.findById(bookId);
    Optional<User> optionalUser = Optional.ofNullable(userService.findUserById(borrowerId));

    if (optionalBook.isEmpty() || optionalUser.isEmpty()) {
      return false;
    }

    Book book = optionalBook.get();
    User borrower = optionalUser.get();

    var bookLending = findBookLendingByBookId(bookId);
    if (bookLending.isEmpty()) {
      System.out.println("Book already reserved");
      eventPublisher.publishBookNotAvailableEvent(this, bookId, borrowerId);
      return false;
    }
    var bl = bookLending.get();
    bl.setBorrower(borrower);
    bl.setReservedAt(LocalDateTime.now());
    bl.setStatus(BookStatus.RESERVED);
    bookLendingRepository.save(bl);

    return true;
  }

  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}", retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class}, backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public boolean cancelBook(Long bookId, Long userId) {
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
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}", retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class}, backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public boolean markAsShipped(Long lendingId) {
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
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}", retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class}, backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public void markAsPending(Long bookId, Long userId) {
    Optional<Book> optBook = bookRepository.findById(bookId);
    User user = userService.findUserById(userId);
    if (optBook.isEmpty()) {
      return;
    }
    Optional<BookLending> optLendings = bookLendingRepository.findByBookAndBorrower(optBook.get(), user);
    if (optLendings.isPresent()) {
      BookLending bookLending = optLendings.get();
      bookLending.setStatus(BookStatus.PENDING_SHIPMENT);
      bookLendingRepository.save(bookLending);
    }
  }

  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}", retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class}, backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public boolean markAsReturn(Long bookId, Long userId) {
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

  public List<BookLending> findUserLendings(Long userId) {
    User user = userService.findUserById(userId);
    return bookLendingRepository.findByBorrower(user);
  }

  @Transactional
  @Retryable(maxAttemptsExpression = "${spring.retry.max-attempts}", retryFor = {ConcurrentModificationException.class,
          ObjectOptimisticLockingFailureException.class}, backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}"))
  public void ListClearBookstoLend(List<BookLending> bookLendings) {
    bookLendingRepository.deleteAll(bookLendings);
  }

  @Transactional
  public List<BookLendingShow> findBorrowedLendingsAsDTO(Long userId) {
    User user = userService.findUserById(userId);
    List<BookLending> borrows = bookLendingRepository.findByBorrower(user);
    System.out.println(borrows);
    for (BookLending borrow : borrows) {
      initializeBookLendingLazyRelations(borrow);
    }
    return borrows.stream()
            .map(bookLendingMapper::toBookLendingShow)
            .collect(Collectors.toList());
  }

  @Transactional
  public List<BookLendingShow> findLentBooksAsDTO(Long userId) {
    System.out.println("Finding lent books ================================= ");
    User user = userService.findUserById(userId);
    List<BookLending> lendings = bookLendingRepository.findBookLendingsByLender(user);
    System.out.println(lendings);

    for (BookLending lending : lendings) {
      initializeBookLendingLazyRelations(lending);
    }

    return lendings.stream()
            .map(bookLendingMapper::toBookLendingShow)
            .collect(Collectors.toList());
  }

  @Transactional
  public LendingResponseDTO findAllUserLendingsAsDTO(Long userId, String type) {
    boolean containsAll = "all".equalsIgnoreCase(type);

    List<BookLendingShow> borrowedBooks = containsAll || "borrowed".equalsIgnoreCase(type)
            ? findBorrowedLendingsAsDTO(userId)
            : List.of();

    List<BookLendingShow> lentBooks = containsAll || "lent".equalsIgnoreCase(type)
            ? findLentBooksAsDTO(userId)
            : List.of();

    return new LendingResponseDTO(borrowedBooks, lentBooks, containsAll);
  }

  public List<BookLending> findUserOwnedLendings(Long userId) {
    User user = userService.findUserById(userId);
    return bookLendingRepository.findBookLendingsByLender(user);
  }

  public Map<String, List<BookLending>> findAllUserLendings(Long userId) {
    User user = userService.findUserById(userId);
    Map<String, List<BookLending>> result = new HashMap<>();

    result.put("borrowed", bookLendingRepository.findByBorrower(user));
    result.put("lent", bookLendingRepository.findBookLendingsByLender(user));

    return result;
  }

  @Transactional
  public void initializeBookLendingLazyRelations(BookLending bookLending) {

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

  @Override
  @Transactional
  public Optional<BookLending> findBookLendingByBookId(Long bookId) {
    return Optional
            .ofNullable(bookLendingRepository.findByBookIdAndBorrowerIsNullAndStatus(bookId, BookStatus.AVAILABLE));
  }

  @Override
  @Transactional
  public boolean loanable(Long bookId, Long userId) {

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
    return true;
  }

  @Override
  @Transactional
  public boolean isNoMoreLoanable(Long bookId, Long userId) {
    Optional<Book> bookOpt = bookRepository.findById(bookId);
    if (bookOpt.isEmpty()) {
      System.out.println("Book not found");
      return false;
    }
    Book book = bookOpt.get();

    User user = userService.findUserById(userId);
    if (user == null) {
      System.out.println("User not found");
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
