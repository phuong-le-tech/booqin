package fr.uge.booqIn.entity;

import fr.uge.booqIn.enums.BookStatus;
import jakarta.persistence.*;

@Entity
public class ToLendBook {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private User owner;

  @ManyToOne(fetch = FetchType.LAZY)
  private Book book;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BookStatus status = BookStatus.AVAILABLE;

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
}
