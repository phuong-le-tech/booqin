package fr.uge.booqIn.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class BookCollection {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String description;

  @ManyToOne
  private User owner;

  @ManyToMany(mappedBy = "collections")
  private List<Book> books = new ArrayList<>();

  @Column(nullable = false)
  private boolean isPublic = false;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_at")
  private Date updatedAt;

  public BookCollection() {
  }

  public BookCollection(String name, String description, User owner) {
    this.title = name;
    this.description = description;
    this.owner = owner;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public User getOwner() {
    return owner;
  }

  public List<Book> getBooks() {
    return books;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setTitle(String name) {
    this.title = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public void setBooks(List<Book> books) {
    this.books = books;
  }

  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void addBook(Book book) {
    if (book.getCollections() == null) {
      book.setCollections(new ArrayList<>()); // ✅ Ajoute une vérification de null
    }
    books.add(book);
    book.getCollections().add(this);
  }

  public void removeBook(Book book) {
    books.remove(book);
    book.getCollections().remove(this);
  }
}
