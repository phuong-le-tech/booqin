package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookLendingRepository extends JpaRepository<BookLending, Long> {
  Optional<BookLending> findById(Long id);
  
  Optional<BookLending> findByBookId(Long bookId);
  
  List<BookLending> findByStatusAndReservedAtBefore(BookStatus status, LocalDateTime expirationTime);
  
  Optional<BookLending> findByBookAndBorrower(Book book, User user);
  
  List<BookLending> findByBorrower(User user);
  
  List<BookLending> findBookLendingsByLender(User lender);
  
  BookLending findByBookIdAndBorrowerIsNullAndStatus(Long bookId, BookStatus status);
  
  @Query("SELECT bl FROM BookLending bl " +
          "WHERE bl.book.id = :bookId " +
          "AND bl.borrower IS NULL " +
          "AND bl.status = fr.uge.booqIn.enums.BookStatus.AVAILABLE " +
          "ORDER BY bl.lender.lendingPoints DESC")
  List<BookLending> findAvailableLendingsForBookOrderByLenderPoints(@Param("bookId") Long bookId);
  
  List<BookLending> findByStatusAndDueDateBefore(BookStatus status, LocalDateTime date);
  
  List<BookLending> findByStatusAndStartDateBefore(BookStatus status, LocalDateTime date);
}

