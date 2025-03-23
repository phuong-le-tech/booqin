package fr.uge.booqIn.entity;


import fr.uge.booqIn.enums.NotificationStatus;
import jakarta.persistence.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "book_notification_queue")
public class BookNotificationQueue {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;
  
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  
  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
  
  private LocalDateTime notifiedAt;
  
  private LocalDateTime reservationExpiresAt;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationStatus status;
  
  @Column(nullable = false)
  
  private Integer position;
  
  
  
  public BookNotificationQueue() {
  }
  
  public BookNotificationQueue(Book book, User user, Integer position) {
    this.book = book;
    this.user = user;
    this.position = position;
    this.createdAt = LocalDateTime.now();
    this.status = NotificationStatus.WAITING;
  }
  
  public Long getId() {
    return id;
  }
  public Book getBook() {
    return book;
  }
  
  public void setBook(Book book) {
    this.book = book;
  }
  
  public User getUser() {
    return user;
  }
  
  public void setUser(User user) {
    this.user = user;
  }
  
  public void setPosition(Integer position) {
    this.position = position;
  }
  
  public void  setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
  
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
  
  public LocalDateTime getNotifiedAt() {
    return notifiedAt;
  }
  
  public void setNotifiedAt(LocalDateTime notifiedAt) {
    this.notifiedAt = notifiedAt;
  }
  
  public LocalDateTime getReservationExpiresAt() {
    return reservationExpiresAt;
  }
  
  public void setReservationExpiresAt(LocalDateTime reservationExpiresAt) {
    this.reservationExpiresAt = reservationExpiresAt;
  }
  
  public NotificationStatus getStatus() {
    return status;
  }
  public void setStatus(NotificationStatus status) {
    this.status = status;
  }
  
  public Integer getPosition() {
    return position;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
}
