package fr.uge.booqIn.dto.notification;

import fr.uge.booqIn.enums.NotificationStatus;
import java.time.LocalDateTime;

public record BookNotificationDTO(
        Long id,
        Long bookId,
        String bookTitle,
        String bookCover,
        String authorNames,
        NotificationStatus status,
        LocalDateTime expiresAt,
        Integer position,
        LocalDateTime createdAt
) {

  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookCover;
    private String authorNames;
    private NotificationStatus status;
    private LocalDateTime expiresAt;
    private Integer position;
    private LocalDateTime createdAt;
    
    public Builder id(Long id) {
      this.id = id;
      return this;
    }
    
    public Builder bookId(Long bookId) {
      this.bookId = bookId;
      return this;
    }
    
    public Builder bookTitle(String bookTitle) {
      this.bookTitle = bookTitle;
      return this;
    }
    
    public Builder bookCover(String bookCover) {
      this.bookCover = bookCover;
      return this;
    }
    
    public Builder authorNames(String authorNames) {
      this.authorNames = authorNames;
      return this;
    }
    
    public Builder status(NotificationStatus status) {
      this.status = status;
      return this;
    }
    
    public Builder expiresAt(LocalDateTime expiresAt) {
      this.expiresAt = expiresAt;
      return this;
    }
    
    public Builder position(Integer position) {
      this.position = position;
      return this;
    }
    
    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }
    
    public BookNotificationDTO build() {
      return new BookNotificationDTO(
              id, bookId, bookTitle, bookCover, authorNames,
              status, expiresAt, position, createdAt
      );
    }
  }
}