package fr.uge.booqIn.entity;

import fr.uge.booqIn.model.Commentable;
import fr.uge.booqIn.model.PolymorphicEntity;
import fr.uge.booqIn.model.Votable;
import jakarta.persistence.*;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
public class BookCollection implements PolymorphicEntity, Commentable, Votable {
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
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_at")
  private Date updatedAt;

  @Column(columnDefinition = "boolean default false")
  private boolean isSmart = false;

  @Enumerated(EnumType.STRING)
  private CollectionType collectionType = CollectionType.NORMAL;

  @Override
  public String toString() {
    return "BookCollection{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", owner=" + owner +
            ", books=" + books.stream().limit(10).toList() +
            ", isPublic=" + isPublic +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            ", isSmart=" + isSmart +
            ", collectionType=" + collectionType +
            '}';
  }

  public enum CollectionType {
    NORMAL, // basic collection
    SMART,  // defined by criteria, by user
    GLOBAL  // generated automatically
  }

  public BookCollection() {
  }

  public BookCollection(String name, String description, User owner) {
    this.title = name;
    this.description = description;
    this.owner = owner;
  }

  private BookCollection(Builder builder) {
    this.title = builder.title;
    this.description = builder.description;
    this.owner = builder.owner;
    this.books = builder.books;
    this.isPublic = builder.isPublic;
    this.createdAt = builder.createdAt;
    this.updatedAt = builder.updatedAt;
    this.isSmart = builder.isSmart;
    this.collectionType = builder.collectionType;
  }

  public static class Builder {
    private Long id;
    private String title;
    private String description;
    private User owner;
    private List<Book> books = new ArrayList<>();
    private boolean isPublic = false;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();
    private boolean isSmart = false;
    private CollectionType collectionType = CollectionType.NORMAL;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder owner(User owner) {
      this.owner = owner;
      return this;
    }

    public Builder books(List<Book> books) {
      this.books = books;
      return this;
    }

    public Builder isPublic(boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }

    public Builder createdAt(Date createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder updatedAt(Date updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public Builder isSmart(boolean isSmart) {
      this.isSmart = isSmart;
      return this;
    }

    public Builder collectionType(CollectionType collectionType) {
      this.collectionType = collectionType;
      return this;
    }

    public BookCollection build() {
      return new BookCollection(this);
    }
  }

  public Long getId() {
    return id;
  }

  public Long getVersion() {
    return version;
  }

  @Override
  public String getEntityType() {
    return ENTITY_TYPE;
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

  public boolean isSmart() {
    return isSmart;
  }

  public CollectionType getCollectionType() {
    return collectionType;
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

  public void setVersion(Long version) {
    this.version = version;
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

  public void setSmart(boolean isSmart) {
    this.isSmart = isSmart;
  }

  public void setCollectionType(CollectionType collectionType) {
    this.collectionType = collectionType;
  }

  public void addBook(Book book) {
    books.add(book);
  }

  public void removeBook(Book book) {
    if (!books.remove(book)) {
      throw new IllegalArgumentException("Book not found in collection");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BookCollection that)) return false;
    return isPublic == that.isPublic &&
            isSmart == that.isSmart &&
            Objects.equals(title, that.title) &&
            Objects.equals(description, that.description) &&
            Objects.equals(owner, that.owner) &&
            Objects.equals(books, that.books) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            collectionType == that.collectionType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, description, owner, books, isPublic, createdAt, updatedAt, isSmart, collectionType);
  }
}
