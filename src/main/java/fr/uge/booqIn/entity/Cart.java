package fr.uge.booqIn.entity;

import jakarta.persistence.*;
import fr.uge.booqIn.enums.StatusPayment;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Cart {

  @Id
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false)
  private Long version;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StatusPayment status = StatusPayment.PENDING;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
          name = "cart_books",
          joinColumns = @JoinColumn(name = "cart_id"),
          inverseJoinColumns = @JoinColumn(name = "book_id")
  )
  private Set<Book> books = new HashSet<>();

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(nullable = true)
  private String paymentIntentId;

  public Cart(StatusPayment status, Set<Book> books) {
    this.status = status;
    this.books = books;
  }

  public Cart() {

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public StatusPayment getStatus() {
    return status;
  }

  public void setStatut(StatusPayment status) {
    this.status = status;
  }

  public Set<Book> getBooks() {
    return books;
  }

  public void setBooks(Set<Book> books) {
    this.books = books;
  }

  public void addBook(Book book) {
    this.books.add(book);
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getPaymentIntentId() {
    return this.paymentIntentId;
  }

  public void setPaymentIntentId(String id) {
    this.paymentIntentId = id;
  }

  @Override
  public String toString() {
    return "Cart{" +
            "id=" + id +
            ", statut=" + status +
            ", books=" + books +
            ", createdAt=" + createdAt +
            '}';
  }
}
