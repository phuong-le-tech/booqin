package fr.uge.booqIn.entity;

import fr.uge.booqIn.enums.LoanStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
public class Loan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private User borrower;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private String loanDate = "";
  
  
  @Column(nullable = false)
  private LocalDate returnDate;

  
  @OneToOne
  private ToLendBook toLendBook;
  
  private LoanStatus status = LoanStatus.PENDING_SHIPMENT;

  public Loan() {
  }

  public Loan(User borrower, LocalDate returnDate , ToLendBook toLendBook) {
    this.borrower = borrower;
    this.returnDate = returnDate;
    this.toLendBook = toLendBook;
  }

  public Long getId() {
    return id;
  }

  public User getBorrower() {
    return borrower;
  }

  public String getLoanDate() {
    return loanDate;
  }

  public LocalDate getReturnDate() {
    return returnDate;
  }

  public void setBorrower(User borrower) {
    this.borrower = borrower;
  }

  public void setReturnDate(LocalDate returnDate) {
    this.returnDate = returnDate;
  }

  public void setLoanDate(String loanDate) {
    this.loanDate = loanDate;
  }
  public ToLendBook getToLendBook() {
    return toLendBook;
  }
  public void setToLendBook(ToLendBook toLendBook) {
    this.toLendBook = toLendBook;
  }
  
  public LoanStatus getStatus() {
    return status;
  }
  public void setStatus(LoanStatus status) {
    this.status = status;
  }
  
}
