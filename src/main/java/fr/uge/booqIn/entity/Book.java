package fr.uge.booqIn.entity;

import jakarta.persistence.*;

import java.util.*;

@Entity
public class Book {

  private Book(Builder builder) {
    this.id = builder.id;
    this.isbns = builder.isbns;
    this.title = builder.title;
    this.authors = builder.authors;
    this.description = builder.description;
    this.publicationDate = builder.publicationDate;
    this.collections = builder.collections;
    this.createdAt = builder.createdAt;
    this.updatedAt = builder.updatedAt;
  }

  public static class Builder {
    private Long id;
    private Set<String> isbns = new HashSet<>();
    private String title;
    private Set<Author> authors = new HashSet<>();
    private String description;
    private String publicationDate;
    private List<BookCollection> collections = new ArrayList<>();
    private Date createdAt = new Date();
    private Date updatedAt = new Date();

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder isbns(List<String> isbns) {
      if (isbns != null) {
        this.isbns = new HashSet<>(isbns);
      } else {
        this.isbns = new HashSet<>();
      }
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder authors(Set<Author> authors) {
      this.authors = authors;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder publicationDate(String publicationDate) {
      this.publicationDate = publicationDate;
      return this;
    }

    public Builder collections(List<BookCollection> collections) {
      this.collections = collections;
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

    public Book build() {
      return new Book(this);
    }
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  @ElementCollection
  @CollectionTable(
          name = "book_isbn",
          joinColumns = @JoinColumn(name = "book_id"),
          uniqueConstraints = @UniqueConstraint(columnNames = "isbn"),
          indexes = @Index(name = "idx_isbn", columnList = "isbn")
  )
  @Column(name = "isbn", unique = true)
  private Set<String> isbns = new HashSet<>();


  @Column(nullable = false, length = 5000)
  private String title;

  @ManyToMany(mappedBy = "books")
  private Set<Author> authors;


  @Column(nullable = false, length = 5000)
  private String description;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "publication_date")
  private String publicationDate;

  @ManyToMany
  private List<BookCollection> collections = new ArrayList<>();


  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Date createdAt = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_at")
  private Date updatedAt = new Date();

  public Book() {
  }

  public Book(String title, Set<Author> authors, String description, String publicationDate) {
    this.title = title;
    this.authors = authors;
    this.description = description;
    this.publicationDate = publicationDate;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public Set<Author> getAuthors() {
    return authors;
  }

  public String getDescription() {
    return description;
  }

  public String getPublicationDate() {
    return publicationDate;
  }

  public List<BookCollection> getCollections() {
    return collections;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setAuthors(Set<Author> authors) {
    this.authors = authors;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setPublicationDate(String publicationDate) {
    this.publicationDate = publicationDate;
  }

  public void setCollections(List<BookCollection> collections) {
    this.collections = collections;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void addCollection(BookCollection collection) {
    if (collections == null) {
      collections = new ArrayList<>();
    }
    // empecher les doublons ?
    collections.add(collection);
    collection.getBooks().add(this);

  }

  public void removeCollection(BookCollection collection) {
    if (collection != null && collection.getBooks() != null) {
      collections.remove(collection);
      collection.getBooks().remove(this);
    }

  }

  public Set<String> getIsbns() {
    return isbns;
  }

  public void setIsbns(Set<String> isbns) {
    this.isbns.addAll(isbns);
  }

  public void addIsbn(String isbn) {
    if (isbn != null) this.isbns.add(isbn);
  }

  @Override
  public String toString() {
    return "Book{" +
            String.join(",", isbns) +
            ", title='" + title + '\'' +
            ", authors='" + authors + '\'' +
            ", description='" + description + '\'' +
            ", publicationDate='" + publicationDate + '\'' +
            '}';
  }
}