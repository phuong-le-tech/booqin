package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.lending.BookLendingShow;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.enums.LendStatus;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Interface for managing book lending operations within the BooqIn application.
 * Provides methods for reservations, confirmations, shipping, and returns.
 */
public interface BookLendingInterface {

  /**
   * Confirms a pending loan request.
   *
   * @param lendingId The ID of the lending to confirm
   * @return true if the loan was successfully confirmed, false otherwise
   */
  boolean confirmLoan(@NonNull Long lendingId);

  /**
   * Marks a borrowed book as returned to its owner.
   *
   * @param lendingId The ID of the lending to mark as returned
   * @return true if the book was successfully marked as returned, false otherwise
   */
  boolean returnBook(@NonNull Long lendingId);

  /**
   * Reserves a book for borrowing by a user.
   *
   * @param bookId The ID of the book to reserve
   * @param userId The ID of the user reserving the book
   * @return true if the book was successfully reserved, false otherwise (e.g., if already reserved)
   */
  boolean reserveBook(@NonNull Long bookId, @NonNull Long userId);

  /**
   * Marks a book as shipped to the borrower.
   *
   * @param lendingId The ID of the lending to mark as shipped
   * @return true if the book was successfully marked as shipped, false otherwise
   */
  boolean markAsShipped(@NonNull Long lendingId);

  /**
   * Marks a book as pending for the lending process.
   *
   * @param bookId The ID of the book to mark as pending
   * @param userId The ID of the user who will receive the book
   */
  void markAsPending(@NonNull Long bookId, @NonNull Long userId);

  /**
   * Marks a book as returned in the system.
   *
   * @param bookId The ID of the book to mark as returned
   * @param userId The ID of the user who borrowed the book
   * @return true if the book was successfully marked as returned, false otherwise
   */
  boolean markAsReturn(@NonNull Long bookId, @NonNull Long userId);

  /**
   * Cancels a book reservation or lending.
   *
   * @param bookId The ID of the book to cancel
   * @param userId The ID of the user who reserved/borrowed the book
   * @return true if the reservation was successfully canceled, false otherwise
   */
  boolean cancelBook(@NonNull Long bookId, @NonNull Long userId);

  /**
   * Retrieves all lendings associated with a specific user.
   *
   * @param userId The ID of the user whose lendings to find
   * @return A list of BookLending entities for the specified user
   */
  List<BookLending> findUserLendings(@NonNull Long userId);

  /**
   * Clears a list of book lendings from the system.
   *
   * @param bookLendings The list of book lendings to clear
   */
  void ListClearBookstoLend(@NonNull List<BookLending> bookLendings);

  /**
   * Finds all books borrowed by a specific user and returns them as DTOs.
   *
   * @param userId The ID of the user who borrowed the books
   * @return A list of BookLendingShow DTOs representing borrowed books
   */
  List<BookLendingShow> findBorrowedLendingsAsDTO(@NonNull Long userId);

  /**
   * Finds all books lent by a specific user and returns them as DTOs.
   *
   * @param userId The ID of the user who lent the books
   * @return A list of BookLendingShow DTOs representing lent books
   */
  List<BookLendingShow> findLentBooksAsDTO(@NonNull Long userId);

  /**
   * Retrieves all user lendings of a specified type (borrowed, lent, or all).
   *
   * @param userId The ID of the user whose lendings to find
   * @param type The type of lendings to retrieve (BORROWED, LENT, or ALL)
   * @return A LendingResponseDTO containing the requested lending information
   */
  LendingResponseDTO findAllUserLendingsAsDTO(@NonNull Long userId, @NonNull LendStatus type);

  /**
   * Finds a book lending by book ID.
   *
   * @param bookId The ID of the book to find the lending for
   * @return An Optional containing the BookLending if found, empty otherwise
   */
  Optional<BookLending> findBookLendingByBookId(@NonNull Long bookId);

  /**
   * Makes a book available for loan by a specific user.
   *
   * @param bookId The ID of the book to make loanable
   * @param userId The ID of the user who owns the book
   * @return true if the book was successfully made available for loan, false otherwise
   */
  boolean loanable(@NonNull Long bookId, @NonNull Long userId);

  /**
   * Removes a book from being available for loan.
   *
   * @param bookId The ID of the book to make no longer loanable
   * @param userId The ID of the user who owns the book
   * @return true if the book was successfully made unavailable for loan, false otherwise
   */
  boolean isNoMoreLoanable(@NonNull Long bookId, @NonNull Long userId);

  /**
   * Finds an available book lending for a specific book.
   *
   * @param bookId The ID of the book to find an available lending for
   * @return An Optional containing the BookLending if found, empty otherwise
   */
  Optional<BookLending> findByBookIdAndBorrowerIsNullAndStatus(@NonNull Long bookId);

  /**
   * Generates shipping ticket bytes for a specific lending.
   *
   * @param lendingId The ID of the lending to generate the shipping ticket for
   * @return Byte array containing the shipping ticket PDF data
   */
  byte[] getShippingTicketBytes(@NonNull Long lendingId);
}