package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookLendingRepository extends JpaRepository<BookLending, Long> {
  /**
   * Finds a book lending by its unique identifier.
   *
   * @param id The unique identifier of the book lending
   * @return An optional containing the book lending if found
   */
  @NonNull
  Optional<BookLending> findById(@NonNull Long id);

  /**
   * Finds a book lending by the book's identifier.
   *
   * @param bookId The unique identifier of the book
   * @return An optional containing the book lending if found
   */
  Optional<BookLending> findByBookId(@NonNull Long bookId);

  /**
   * Finds book lendings with a specific status that were reserved before a given time.
   *
   * @param status         The status of the book lending
   * @param expirationTime The time before which the reservations were made
   * @return A list of book lendings matching the criteria
   */
  List<BookLending> findByStatusAndReservedAtBefore(
          @NonNull BookStatus status,
          @NonNull LocalDateTime expirationTime
  );

  /**
   * Finds a book lending for a specific book and borrower.
   *
   * @param book The book being lent
   * @param user The borrower of the book
   * @return An optional containing the book lending if found
   */
  Optional<BookLending> findByBookAndBorrower(
          @NonNull Book book,
          @NonNull User user
  );

  /**
   * Finds all book lendings for a specific borrower.
   *
   * @param user The borrower
   * @return A list of book lendings for the user
   */
  List<BookLending> findByBorrower(@NonNull User user);

  /**
   * Finds all book lendings for a specific lender.
   *
   * @param lender The lender of the books
   * @return A list of book lendings by the lender
   */
  List<BookLending> findBookLendingsByLender(@NonNull User lender);

  /**
   * Finds a book lending for a specific book that is available and has no borrower.
   *
   * @param bookId The unique identifier of the book
   * @param status The status of the book lending
   * @return A book lending matching the criteria
   */
  BookLending findByBookIdAndBorrowerIsNullAndStatus(
          @NonNull Long bookId,
          @NonNull BookStatus status
  );

  /**
   * Finds available book lendings for a specific book, ordered by the lender's lending points.
   *
   * @param bookId The unique identifier of the book
   * @return A list of available book lendings, sorted by lender's lending points
   */
  @Query("SELECT bl FROM BookLending bl " +
          "WHERE bl.book.id = :bookId " +
          "AND bl.borrower IS NULL " +
          "AND bl.status = fr.uge.booqIn.enums.BookStatus.AVAILABLE " +
          "ORDER BY bl.lender.lendingPoints DESC")
  List<BookLending> findAvailableLendingsForBookOrderByLenderPoints(
          @NonNull Long bookId
  );

  /**
   * Finds book lendings with a specific status that are due before a given date.
   *
   * @param status The status of the book lending
   * @param date   The date before which the lendings are due
   * @return A list of book lendings matching the criteria
   */
  List<BookLending> findByStatusAndDueDateBefore(
          @NonNull BookStatus status,
          @NonNull LocalDateTime date
  );

  /**
   * Finds book lendings with a specific status that started before a given date.
   *
   * @param status The status of the book lending
   * @param date   The date before which the lendings started
   * @return A list of book lendings matching the criteria
   */
  List<BookLending> findByStatusAndStartDateBefore(
          @NonNull BookStatus status,
          @NonNull LocalDateTime date
  );
}