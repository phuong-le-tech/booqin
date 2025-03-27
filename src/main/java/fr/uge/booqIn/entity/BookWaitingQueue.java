package fr.uge.booqIn.entity;


import fr.uge.booqIn.enums.QueueStatus;
import jakarta.persistence.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "book_waiting_queue",
        uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "user_id"}))
public class BookWaitingQueue {
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

  @Column
  private LocalDateTime notifiedAt;

  @Column
  private LocalDateTime reservationExpiresAt;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private QueueStatus status;
  
  @Column(nullable = false)
  private Integer position;

  public BookWaitingQueue() {
  }
  
  public BookWaitingQueue(Book book, User user, Integer position) {
    this.book = book;
    this.user = user;
    this.position = position;
    this.createdAt = LocalDateTime.now();
    this.status = QueueStatus.WAITING;
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
  
  public QueueStatus getStatus() {
    return status;
  }
  public void setStatus(QueueStatus status) {
    this.status = status;
  }
  
  public Integer getPosition() {
    return position;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
}
