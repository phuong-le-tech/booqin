package fr.uge.booqIn.entity;

import fr.uge.booqIn.model.Commentable;
import fr.uge.booqIn.model.PolymorphicEntity;
import fr.uge.booqIn.model.Votable;
import jakarta.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * Entity representing a book.
 */
@Entity
public class Book implements PolymorphicEntity, Votable, Commentable {
  private static final String ENTITY_TYPE = "BOOK";

  // -------------------- FIELDS --------------------

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  private Long version;

  @ElementCollection(fetch = FetchType.LAZY)
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

  @ManyToMany
  @JoinTable(
          name = "book_author",
          joinColumns = @JoinColumn(name = "book_id"),
          inverseJoinColumns = @JoinColumn(name = "author_id")
  )
  private Set<Author> authors = new HashSet<>();

  @Column(nullable = false, length = 5000)
  private String description;

  @ElementCollection
  @CollectionTable(
          name = "book_category",
          joinColumns = @JoinColumn(name = "book_id")
  )
  @Column(name = "category")
  private Set<String> categories = new HashSet<>();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "publication_date")
  private Date publicationDate;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Date createdAt = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_at")
  private Date updatedAt = new Date();

  // -------------------- CONSTRUCTORS --------------------

  /**
   * Default constructor required by JPA.
   */
  public Book() {
  }

  /**
   * Standard constructor for creating a book with essential information.
   *
   * @param title The book title
   * @param authors The book authors
   * @param description Book description
   * @param publicationDate Publication date
   */
  public Book(String title, Set<Author> authors, String description, Date publicationDate) {
    Objects.requireNonNull(title);
    Objects.requireNonNull(authors);
    Objects.requireNonNull(description);
    Objects.requireNonNull(publicationDate);
    this.title = title;
    this.authors = authors;
    this.description = description;
    this.publicationDate = publicationDate;
  }

  /**
   * Private constructor used by the Builder.
   */
  private Book(Builder builder) {
    this.id = builder.id;
    this.isbns = builder.isbns;
    this.title = builder.title;
    this.authors = builder.authors;
    this.description = builder.description;
    this.categories = builder.categories;
    this.publicationDate = builder.publicationDate;
    this.createdAt = builder.createdAt;
    this.updatedAt = builder.updatedAt;
  }

  // -------------------- GETTERS --------------------

  @Override
  public String getEntityType() {
    return ENTITY_TYPE;
  }

  public Long getId() {
    return id;
  }

  public Long getVersion() {
    return version;
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

  public Set<String> getCategories() {
    return categories;
  }

  public Date getPublicationDate() {
    return publicationDate;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public Set<String> getIsbns() {
    return isbns;
  }

  // -------------------- SETTERS --------------------

  public void setId(Long id) {
    this.id = id;
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

  public void setCategories(Set<String> categories) {
    this.categories = categories;
  }

  public void setPublicationDate(Date publicationDate) {
    this.publicationDate = publicationDate;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setIsbns(Set<String> isbns) {
    this.isbns.addAll(isbns);
  }

  // -------------------- UTILITY METHODS --------------------

  /**
   * Adds a single ISBN to the book.
   *
   * @param isbn The ISBN to add
   */
  public void addIsbn(String isbn) {
    if (isbn != null) this.isbns.add(isbn);
  }

  /**
   * Adds a single category to the book.
   *
   * @param category The category to add
   */
  public void addCategory(String category) {
    if (category != null) this.categories.add(category);
  }

  @Override
  public String toString() {
    return "Book{" +
            "id=" + id +
            ", isbns=" + (isbns.isEmpty() ? "none" : String.join(",", isbns)) +
            ", title='" + title + '\'' +
            ", authors=" + authors +
            ", categories=" + categories +
            ", publicationDate=" + publicationDate +
            '}';
  }

  // -------------------- BUILDER CLASS --------------------

  /**
   * Builder class for creating Book instances.
   */
  public static class Builder {
    private Long id;
    private Set<String> isbns = new HashSet<>();
    private String title;
    private Set<Author> authors = new HashSet<>();
    private String description;
    private Set<String> categories = new HashSet<>();
    private Date publicationDate;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder isbns(Set<String> isbns) {
      Objects.requireNonNull(isbns);
      this.isbns = isbns;
      return this;
    }

    public Builder title(String title) {
      Objects.requireNonNull(title);
      this.title = title;
      return this;
    }

    public Builder authors(Set<Author> authors) {
      Objects.requireNonNull(authors);
      this.authors = authors;
      return this;
    }

    public Builder description(String description) {
      Objects.requireNonNull(description);
      this.description = description;
      return this;
    }

    public Builder categories(Set<String> categories) {
      Objects.requireNonNull(categories);
      this.categories = categories;
      return this;
    }

    public Builder publicationDate(Date publicationDate) {
      this.publicationDate = publicationDate;
      return this;
    }

    public Builder createdAt(Date createdAt) {
      Objects.requireNonNull(createdAt);
      this.createdAt = createdAt;
      return this;
    }

    public Builder updatedAt(Date updatedAt) {
      Objects.requireNonNull(updatedAt);
      this.updatedAt = updatedAt;
      return this;
    }

    public Book build() {
      return new Book(this);
    }
  }
}