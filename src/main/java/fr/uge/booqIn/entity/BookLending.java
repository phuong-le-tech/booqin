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
  private LocalDateTime reservedAt;
  
  @Column
  private LocalDateTime startDate;
  
  @Column
  private LocalDateTime dueDate;
  
  @Column
  private LocalDateTime returnDate;
  
  @Lob
  @Column(name = "shipping_ticket_pdf", columnDefinition = "LONGBLOB")
  private byte[] shippingTicketPDF;
  
  public BookLending() {
  }
  
  public BookLending(Book book, User lender) {
    this.book = book;
    this.lender = lender;
    this.reservedAt = LocalDateTime.now();
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
  
  public byte[] getShippingTicketPDF() {
    return shippingTicketPDF;
  }
  
  public void setShippingTicketPDF(byte[] shippingTicketPDF) {
    this.shippingTicketPDF = shippingTicketPDF;
  }
  
  
}
