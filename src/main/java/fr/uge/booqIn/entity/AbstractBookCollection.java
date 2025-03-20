package fr.uge.booqIn.entity;

import fr.uge.booqIn.model.BookCollection;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "collection_type", discriminatorType = DiscriminatorType.STRING)
public abstract class AbstractBookCollection implements BookCollection {
  private static final String ENTITY_TYPE = "COLLECTION";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false)
  private Long version;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  private User owner;

  @ManyToMany(fetch = FetchType.LAZY)
  private List<Book> books = new ArrayList<>();

  @Column(nullable = false)
  private boolean isPublic = false;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Date createdAt = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_at")
  private Date updatedAt = new Date();

  @Transient
  @Override
  public abstract BookCollection.CollectionType getCollectionType();

  protected AbstractBookCollection() {
  }

  protected AbstractBookCollection(String title, String description, User owner, boolean isPublic) {
    this.title = title;
    this.description = description;
    this.owner = owner;
    this.isPublic = isPublic;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public Long getVersion() {
    return version;
  }

  @Override
  public String getEntityType() {
    return ENTITY_TYPE;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public User getOwner() {
    return owner;
  }

  @Override
  public List<Book> getBooks() {
    return books;
  }

  @Override
  public boolean isPublic() {
    return isPublic;
  }

  @Override
  public Date getCreatedAt() {
    return createdAt;
  }

  @Override
  public Date getUpdatedAt() {
    return updatedAt;
  }

  // Setters
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public void setVersion(Long version) {
    this.version = version;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public void setOwner(User owner) {
    this.owner = owner;
  }

  @Override
  public void setBooks(List<Book> books) {
    this.books = books;
  }

  @Override
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  @Override
  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  // Opérations sur les collections
  @Override
  public void addBook(Book book) {
    if (books.contains(book)) {
      throw new IllegalArgumentException("Book already in collection");
    }
    books.add(book);
    this.setUpdatedAt(new Date());
  }

  @Override
  public void removeBook(Book book) {
    if (!books.remove(book)) {
      throw new IllegalArgumentException("Book not found in collection");
    }
    this.setUpdatedAt(new Date());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AbstractBookCollection that)) return false;
    return isPublic == that.isPublic &&
            Objects.equals(title, that.title) &&
            Objects.equals(description, that.description) &&
            Objects.equals(owner, that.owner) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, description, owner, isPublic, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", owner=" + owner +
            ", books=" + books.stream().limit(10).toList() +
            ", isPublic=" + isPublic +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
  }
}