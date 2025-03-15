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
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
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
  private final  BookServiceImpl bookService;
  
  public BookLendingServiceImpl(BookLendingRepository bookLendingRepository , UserServiceImpl userService , BookRepository bookRepository, BookLendingMapper bookLendingMapper, BookServiceImpl bookService) {
    this.bookLendingRepository = bookLendingRepository;
    this.userService = userService;
    this.bookRepository = bookRepository;
    this.bookLendingMapper = bookLendingMapper;
    this.bookService = bookService;
  }
  
  
  @Transactional
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
  
  public boolean cancelBook(Long bookId , Long userId) {
    User user = userService.findUserById(userId);
    Optional<Book> optBook = bookRepository.findById(bookId);
    if (optBook.isEmpty()) {
      return false;
    }
    Optional<BookLending> optLendings = bookLendingRepository.findByBookAndBorrower(optBook.get() , user);
    if (optLendings.isPresent()) {
      BookLending bookLending = optLendings.get();
      bookLendingRepository.delete(bookLending);
      return true;
    }
    return false;
  }
  
  @Transactional
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
    return true;
  }
  
  public void markAsPending(Long bookId , Long userId) {
    Optional<Book> optBook = bookRepository.findById(bookId);
    User user = userService.findUserById(userId);
    if (optBook.isEmpty()) {
      return;
    }
    Optional<BookLending> optLendings = bookLendingRepository.findByBookAndBorrower(optBook.get() , user);
    if (optLendings.isPresent()) {
      BookLending bookLending = optLendings.get();
      bookLending.setStatus(BookStatus.PENDING_SHIPMENT);
      bookLendingRepository.save(bookLending);
    }
  }
  
  
  @Transactional
  public boolean markAsReturn(Long bookId , Long userId) {
    Optional<Book> optBook = bookRepository.findById(bookId);
    User user = userService.findUserById(userId);
    if (optBook.isEmpty()) {
      return false;
    }
    Optional<BookLending> optLendings = bookLendingRepository.findByBookAndBorrower(optBook.get() , user);
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
  public void ListClearBookstoLend(List<BookLending> bookLendings){
    bookLendingRepository.deleteAll(bookLendings);
  }
  
  @Transactional
  public List<BookLendingShow> findBorrowedLendingsAsDTO(Long userId) {
    User user = userService.findUserById(userId);
    List<BookLending> lendings = bookLendingRepository.findByBorrower(user);
    for (BookLending lending : lendings) {
      initializeBookLendingLazyRelations(lending);
    }
    return lendings.stream()
            .map(bookLendingMapper::toBookLendingShow)
            .collect(Collectors.toList());
  }
  
  @Transactional
  public List<BookLendingShow> findLentBooksAsDTO(Long userId) {
    User user = userService.findUserById(userId);
    List<BookLending> lendings = bookLendingRepository.findByLender(user);
    
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
    return bookLendingRepository.findByLender(user);
  }

  public Map<String, List<BookLending>> findAllUserLendings(Long userId) {
    User user = userService.findUserById(userId);
    Map<String, List<BookLending>> result = new HashMap<>();
    
    result.put("borrowed", bookLendingRepository.findByBorrower(user));
    result.put("lent", bookLendingRepository.findByLender(user));
    
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
  
  
}

