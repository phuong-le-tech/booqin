package fr.uge.booqIn.event;

public class BookReturnedEvent {
  private final Long bookId;
  
  public BookReturnedEvent(Long bookId) {
    this.bookId = bookId;
  }
  
  public Long getBookId() {
    return bookId;
  }
}