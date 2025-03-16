package fr.uge.booqIn.event;

public class BookNotAvailableEvent {
  private final Long bookId;
  private final Long userId;
  
  public BookNotAvailableEvent(Long bookId, Long userId) {
    this.bookId = bookId;
    this.userId = userId;
  }
  
  public Long getBookId() {
    return bookId;
  }
  
  public Long getUserId() {
    return userId;
  }
}