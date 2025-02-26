package fr.uge.booqIn.entity;

import jakarta.persistence.*;
import fr.uge.booqIn.enums.StatutPayment;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Cart {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPayment statut;

    @ManyToMany
    @JoinTable(name = "cart_books", joinColumns = @JoinColumn(name = "cart_id"), inverseJoinColumns = @JoinColumn(name = "book_id"))
    private List<Book> books;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = true)
    private String paymentIntentId;

    public Cart(User user, StatutPayment statut, List<Book> books) {
        this.user = user;
        this.statut = statut;
        this.books = books;
    }
    
    public Cart() {
    }
    
    public StatutPayment getStatut() {
        return statut;
    }

    public void setStatut(StatutPayment statut) {
        this.statut = statut;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void getPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }
    public String setPaymentIntentId() {
        return paymentIntentId;
    }
    
    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", user=" + user +
                ", statut=" + statut +
                ", books=" + books +
                ", createdAt=" + createdAt +
                '}';
    }
}
