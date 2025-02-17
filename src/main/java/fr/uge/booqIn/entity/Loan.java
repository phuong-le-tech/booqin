package fr.uge.booqIn.entity;

import jakarta.persistence.*;

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

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private String returnDate;

  public Loan() {
  }

  public Loan(User borrower, String returnDate) {
    this.borrower = borrower;
    this.returnDate = returnDate;
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

  public String getReturnDate() {
    return returnDate;
  }

  public void setBorrower(User borrower) {
    this.borrower = borrower;
  }

  public void setReturnDate(String returnDate) {
    this.returnDate = returnDate;
  }

  public void setLoanDate(String loanDate) {
    this.loanDate = loanDate;
  }
}
