package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.entity.BookLending;

import java.util.List;

public interface BookLendingInterface {
  boolean confirmLoan(Long lendingId);
  boolean returnBook(Long lendingId);
  boolean reserveBook(Long bookId, Long userId);
  boolean markAsShipped(Long bookId , Long userId);
  boolean cancelBook(Long bookId, Long userId);
  List<BookLending> findUserLendings(Long userId);
  void ListClearBookstoLend(List<BookLending> bookLendings);
}
