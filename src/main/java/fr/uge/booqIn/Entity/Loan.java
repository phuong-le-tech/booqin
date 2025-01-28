package fr.uge.booqIn.Entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Loan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private User borrower;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Date loanDate = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Date returnDate;

  public Loan() {
  }

  public Loan(User borrower, Date returnDate) {
    this.borrower = borrower;
    this.returnDate = returnDate;
  }

  public Long getId() {
    return id;
  }

  public User getBorrower() {
    return borrower;
  }

  public Date getLoanDate() {
    return loanDate;
  }

  public Date getReturnDate() {
    return returnDate;
  }

  public void setBorrower(User borrower) {
    this.borrower = borrower;
  }

  public void setReturnDate(Date returnDate) {
    this.returnDate = returnDate;
  }

  public void setLoanDate(Date loanDate) {
    this.loanDate = loanDate;
  }
}
