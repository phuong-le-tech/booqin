package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.repository.BookLendingRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookLendingServiceImpl implements BookLendingInterface {
  
  private final BookLendingRepository bookLendingRepository;
  private final UserServiceImpl userService;
  private final BookRepository bookRepository;
  
  public BookLendingServiceImpl(BookLendingRepository bookLendingRepository , UserServiceImpl userService , BookRepository bookRepository) {
    this.bookLendingRepository = bookLendingRepository;
    this.userService = userService;
    this.bookRepository = bookRepository;
  }
  
  
  @Transactional
  public boolean confirmLoan(Long lendingId) {
    Optional<BookLending> optionalLending = bookLendingRepository.findById(lendingId);
    
    if (optionalLending.isEmpty()) {
      return false;
    }
    
    BookLending lending = optionalLending.get();
    
    if (lending.getStatus() != BookStatus.RESERVED) {
      return false;
    }

    lending.setStatus(BookStatus.BORROWED);
    lending.setStartDate(LocalDateTime.now());
    lending.setDueDate(lending.getStartDate().plusWeeks(2));
    
    bookLendingRepository.save(lending);
    
    return true;
  }
  
  
  @Transactional
  public boolean returnBook(Long lendingId) {
    Optional<BookLending> optionalLending = bookLendingRepository.findById(lendingId);
    
    if (optionalLending.isEmpty()) {
      return false;
    }
    
    BookLending lending = optionalLending.get();
    
    if (lending.getStatus() != BookStatus.RETURNED) {
      return false;
    }

    lending.setStatus(BookStatus.AVAILABLE);
    lending.setReturnDate(LocalDateTime.now());
    lending.setBorrower(null);
    
    bookLendingRepository.save(lending);
    
    return true;
  }
  
  
  @Transactional
  public boolean reserveBook(Long bookId, Long borrowerId) {
    Optional<Book> optionalBook = bookRepository.findById(bookId);
    Optional<User> optionalUser = Optional.ofNullable(userService.findUserById(borrowerId));
    
    if (optionalBook.isEmpty() || optionalUser.isEmpty()) {
      return false;
    }
    
    Book book = optionalBook.get();
    User borrower = optionalUser.get();
    

    Optional<BookLending> existingLoan = bookLendingRepository.findByBookId(bookId);
    
    if (existingLoan.isPresent()) {
      BookLending lending = existingLoan.get();
      if (lending.getStatus() != BookStatus.AVAILABLE) {
        return false;
      }
    }
    BookLending newLoan = new BookLending(book, borrower, borrower);
    bookLendingRepository.save(newLoan);
    
    return true;
  }
  
  @Transactional
  public void receiveBook(Long bookId , Long userId) {
    Optional<Book> optBook = bookRepository.findById(bookId);
    User user = userService.findUserById(userId);
    if (optBook.isEmpty()) {
      return;
    }
    Optional<BookLending> optLendings = bookLendingRepository.findByBookAndBorrower(optBook.get() , user);
    if (optLendings.isPresent()) {
      BookLending bookLending = optLendings.get();
      if (bookLending.getStatus() == BookStatus.SHIPPED) {
        bookLending.setStatus(BookStatus.BORROWED);
        bookLendingRepository.save(bookLending);
      }
    }
  }
  
  
  // changer de place
  @Scheduled(fixedRate = 60000)
  @Transactional
  public void expireReservations() {
    LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(20);
    List<BookLending> expiredReservations = bookLendingRepository.findByStatusAndReservedAtBefore(BookStatus.RESERVED, expirationTime);
    for (BookLending lending : expiredReservations) {
      lending.setStatus(BookStatus.AVAILABLE);
      lending.setReservedAt(null);
      lending.setBorrower(null);
      bookLendingRepository.save(lending);
    }
  }
  
  
  @Transactional
  public boolean markAsShipped(Long bookId , Long userId) {
    Optional<Book> optBook = bookRepository.findById(bookId);
    User user = userService.findUserById(userId);
    if (optBook.isEmpty()) {
      return false;
    }
    Optional<BookLending> optLendings = bookLendingRepository.findByBookAndBorrower(optBook.get() , user);
    if (optLendings.isPresent()) {
      BookLending bookLending = optLendings.get();
      if (bookLending.getStatus() == BookStatus.RESERVED) {
        bookLending.setStatus(BookStatus.SHIPPED);
        bookLendingRepository.save(bookLending);
        return true;
      }
    }
    return false;
  }
  
}
