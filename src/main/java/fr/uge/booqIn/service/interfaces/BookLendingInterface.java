package fr.uge.booqIn.service.interfaces;

public interface BookLendingInterface {
  boolean confirmLoan(Long lendingId);
  boolean returnBook(Long lendingId);
  boolean reserveBook(Long bookId, Long userId);
  boolean markAsShipped(Long bookId , Long userId);
  
}
