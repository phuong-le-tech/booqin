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

  @Column(columnDefinition = "boolean default false")
  private boolean isSmart = false;

  @Enumerated(EnumType.STRING)
  private CollectionType collectionType = CollectionType.NORMAL;

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
    private String title;
    private String description;
    private User owner;
    private List<Book> books = new ArrayList<>();
    private boolean isPublic = false;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();
    private boolean isSmart = false;
    private CollectionType collectionType = CollectionType.NORMAL;

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

  public void setSmart(boolean isSmart) {
    this.isSmart = isSmart;
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
