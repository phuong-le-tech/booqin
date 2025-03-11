package fr.uge.booqIn.entity;

import fr.uge.booqIn.enums.BookStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class BookLending {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  private Book book;
  
  @ManyToOne(fetch = FetchType.LAZY)
  private User borrower;
  
  @ManyToOne(fetch = FetchType.LAZY)
  private User lender;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BookStatus status = BookStatus.AVAILABLE;
  
  @Column
  private LocalDateTime reservedAt;  // ⏳ Gère la réservation temporaire (20 min)
  
  @Column
  private LocalDateTime startDate; // 📅 Date de début du prêt
  
  @Column
  private LocalDateTime dueDate; // 📅 Date limite de retour
  
  @Column
  private LocalDateTime returnDate; // 📅 Date effective de retour
  
  public BookLending() {
  }
  
  // 📌 Constructeur utilisé lors de la création d'un prêt
  public BookLending(Book book, User lender, User borrower) {
    this.book = book;
    this.lender = lender;
    this.borrower = borrower;
    this.status = BookStatus.RESERVED;
    this.reservedAt = LocalDateTime.now(); // Réservation immédiate
  }
  
  public Long getId() {
    return id;
  }
  
  public User getBorrower() {
    return borrower;
  }
  
  public User getLender() {
    return lender;
  }
  
  public Book getBook() {
    return book;
  }
  
  public LocalDateTime getStartDate() {
    return startDate;
  }
  
  public LocalDateTime getDueDate() {
    return dueDate;
  }
  
  public LocalDateTime getReturnDate() {
    return returnDate;
  }
  
  public LocalDateTime getReservedAt() {
    return reservedAt;
  }
  
  public BookStatus getStatus() {
    return status;
  }
  
  public void setBorrower(User borrower) {
    this.borrower = borrower;
  }
  
  public void setLender(User lender) {
    this.lender = lender;
  }
  
  public void setStartDate(LocalDateTime startDate) {
    this.startDate = startDate;
  }
  
  public void setDueDate(LocalDateTime dueDate) {
    this.dueDate = dueDate;
  }
  
  public void setReturnDate(LocalDateTime returnDate) {
    this.returnDate = returnDate;
  }
  
  public void setReservedAt(LocalDateTime reservedAt) {
    this.reservedAt = reservedAt;
  }
  
  public void setStatus(BookStatus status) {
    this.status = status;
  }

  public boolean isReservationExpired() {
    return reservedAt != null && reservedAt.plusMinutes(20).isBefore(LocalDateTime.now());
  }
  

}
