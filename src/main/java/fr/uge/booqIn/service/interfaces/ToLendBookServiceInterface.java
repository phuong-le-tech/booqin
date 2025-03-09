package fr.uge.booqIn.service.interfaces;

public interface ToLendBookServiceInterface {
  public boolean reserveBook(Long bookId, Long userId);
  public void expireReservations();
}
