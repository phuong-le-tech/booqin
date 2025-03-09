package fr.uge.booqIn.entity;

import fr.uge.booqIn.enums.BookStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ToLendBook {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private User owner;

  @ManyToOne(fetch = FetchType.LAZY)
  private Book book;
  
  @ManyToOne(fetch = FetchType.LAZY)
  private User borrower;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BookStatus status = BookStatus.AVAILABLE;
  
  
  private LocalDateTime reservedAt;
  
  
  public ToLendBook() {
  }

  
  public ToLendBook(User owner, Book book) {
    this.owner = owner;
    this.book = book;
  }

  public Long getId() {
    return id;
  }

  public User getOwner() {
    return owner;
  }

  public Book getBook() {
    return book;
  }

  public BookStatus getStatus() {
    return status;
  }

  public void setStatus(BookStatus status) {
    this.status = status;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public void setBook(Book book) {
    this.book = book;
  }
  
  public User getBorrower() {
    return borrower;
  }
  
  public void setBorrower(User borrower) {
    this.borrower = borrower;
  }
  
  public LocalDateTime getReservedAt() {
    return reservedAt;
  }
  
  public void setReservedAt(LocalDateTime reservedAt) {
    this.reservedAt = reservedAt;
  }
}
