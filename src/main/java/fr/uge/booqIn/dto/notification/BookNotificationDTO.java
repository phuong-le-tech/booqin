package fr.uge.booqIn.dto.notification;

import fr.uge.booqIn.enums.NotificationStatus;
import java.time.LocalDateTime;

public class BookNotificationDTO {
  private Long id;
  private Long bookId;
  private String bookTitle;
  private String bookCover;
  private String authorNames;
  private NotificationStatus status;
  private LocalDateTime expiresAt;
  private Integer position;
  private LocalDateTime createdAt;
  
  // Constructeur par défaut
  public BookNotificationDTO() {
  }
  
  // Constructeur avec tous les champs
  public BookNotificationDTO(Long id, Long bookId, String bookTitle, String bookCover,
                             String authorNames, NotificationStatus status,
                             LocalDateTime expiresAt, Integer position, LocalDateTime createdAt) {
    this.id = id;
    this.bookId = bookId;
    this.bookTitle = bookTitle;
    this.bookCover = bookCover;
    this.authorNames = authorNames;
    this.status = status;
    this.expiresAt = expiresAt;
    this.position = position;
    this.createdAt = createdAt;
  }
  
  // Getters et setters
  public Long getId() {
    return id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public Long getBookId() {
    return bookId;
  }
  
  public void setBookId(Long bookId) {
    this.bookId = bookId;
  }
  
  public String getBookTitle() {
    return bookTitle;
  }
  
  public void setBookTitle(String bookTitle) {
    this.bookTitle = bookTitle;
  }
  
  public String getBookCover() {
    return bookCover;
  }
  
  public void setBookCover(String bookCover) {
    this.bookCover = bookCover;
  }
  
  public String getAuthorNames() {
    return authorNames;
  }
  
  public void setAuthorNames(String authorNames) {
    this.authorNames = authorNames;
  }
  
  public NotificationStatus getStatus() {
    return status;
  }
  
  public void setStatus(NotificationStatus status) {
    this.status = status;
  }
  
  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }
  
  public void setExpiresAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }
  
  public Integer getPosition() {
    return position;
  }
  
  public void setPosition(Integer position) {
    this.position = position;
  }
  
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
  
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
  
  // Méthode builder statique pour la création d'instances
  public static Builder builder() {
    return new Builder();
  }
  
  // Classe Builder interne pour le pattern Builder
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