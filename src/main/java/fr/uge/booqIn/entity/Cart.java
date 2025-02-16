package fr.uge.booqIn.entity;

import jakarta.persistence.*;
import fr.uge.booqIn.enums.Statut;
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
    private Statut statut;

    @ManyToMany
    @JoinTable(name = "cart_books", joinColumns = @JoinColumn(name = "cart_id"), inverseJoinColumns = @JoinColumn(name = "book_id"))
    private List<Book> books;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Cart(User user, Statut statut, List<Book> books) {
        this.user = user;
        this.statut = statut;
        this.books = books;
    }
    
    public Cart() {
    }
    
    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
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
