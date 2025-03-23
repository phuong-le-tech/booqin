package fr.uge.booqIn.entity;

import fr.uge.booqIn.model.BookCollection;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

  @NotBlank(message = "Collection title cannot be empty")
  @Length(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
  @Column(nullable = false)
  private String title;

  @NotBlank(message = "Collection description cannot be empty")
  @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @NotNull(message = "Collection must have an owner")
  @ManyToOne(fetch = FetchType.LAZY)
  private User owner;

  @ManyToMany(fetch = FetchType.LAZY)
  private List<Book> books = new ArrayList<>();

  @Column(nullable = false)
  private boolean isPublic = false;

  @NotNull(message = "Creation date is required")
  @PastOrPresent(message = "Creation date cannot be in the future")
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @PastOrPresent(message = "Update date cannot be in the future")
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

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
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public LocalDateTime getUpdatedAt() {
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
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  // Collection operations
  @Override
  public void addBook(Book book) {
    if (books.contains(book)) {
      throw new IllegalArgumentException("Book already in collection");
    }
    books.add(book);
    this.setUpdatedAt(LocalDateTime.now());
  }

  @Override
  public void removeBook(Book book) {
    if (!books.remove(book)) {
      return;
    }
    this.setUpdatedAt(LocalDateTime.now());
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