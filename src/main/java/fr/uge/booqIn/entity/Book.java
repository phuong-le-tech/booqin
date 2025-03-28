package fr.uge.booqIn.entity;

import fr.uge.booqIn.interfaces.Commentable;
import fr.uge.booqIn.interfaces.Followable;
import fr.uge.booqIn.interfaces.PolymorphicEntity;
import fr.uge.booqIn.interfaces.Votable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Book implements PolymorphicEntity, Votable, Commentable, Followable {
  private static final String ENTITY_TYPE = "BOOK";

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
  private Set</*@ISBN(message = "Invalid ISBN format")*/ String> isbns = new HashSet<>();

  @NotBlank(message = "Book title cannot be empty")
  @Length(min = 1, max = 5000, message = "Title must be between 1 and 5000 characters")
  @Column(nullable = false, length = 5000)
  private String title;

  @ManyToMany
  @JoinTable(
          name = "book_author",
          joinColumns = @JoinColumn(name = "book_id"),
          inverseJoinColumns = @JoinColumn(name = "author_id"),
          indexes = {
                  @Index(name = "idx_book_author_book_id", columnList = "book_id"),
                  @Index(name = "idx_book_author_author_id", columnList = "author_id")
          }
  )
  private Set<Author> authors = new HashSet<>();

  @Length(max = 5000, message = "Description must be between 0 and 5000 characters")
  @Column(nullable = false, length = 5000)
  private String description;

  @ElementCollection
  @CollectionTable(
          name = "book_category",
          joinColumns = @JoinColumn(name = "book_id"),
          indexes = @Index(name = "idx_book_category_book_id", columnList = "book_id")
  )
  @Column(name = "category")
  private Set<String> categories = new HashSet<>();

  @Column(name = "publication_date")
  private Date publicationDate;

  @NotNull(message = "Creation date is required")
  @PastOrPresent(message = "Creation date cannot be in the future")
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @PastOrPresent(message = "Update date cannot be in the future")
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "cover_image_path")
  private String coverImagePath;

  @Column(name = "thumbnail_path")
  private String thumbnailPath;

  @Column(name = "cover_image_url")
  private String coverImageUrl;

  public Book() {
  }

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
    this.coverImagePath = builder.coverImagePath;
    this.thumbnailPath = builder.thumbnailPath;
    this.coverImageUrl = builder.coverImageUrl;
  }

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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public Set<String> getIsbns() {
    return isbns;
  }

  public String getCoverImagePath() {
    return coverImagePath;
  }

  public String getThumbnailPath() {
    return thumbnailPath;
  }

  public String getCoverImageUrl() {
    return coverImageUrl;
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

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setIsbns(Set<String> isbns) {
    this.isbns.addAll(isbns);
  }

  public void setCoverImagePath(String coverImagePath) {
    this.coverImagePath = coverImagePath;
  }
  public void setThumbnailPath(String thumbnailPath) {
    this.thumbnailPath = thumbnailPath;
  }
  public void setCoverImageUrl(String coverImageUrl) {
    this.coverImageUrl = coverImageUrl;
  }

  public void addIsbn(String isbn) {
    if (isbn != null) this.isbns.add(isbn);
  }

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

  public static class Builder {
    private Long id;
    private Set<String> isbns = new HashSet<>();
    private String title;
    private Set<Author> authors = new HashSet<>();
    private String description;
    private Set<String> categories = new HashSet<>();
    private Date publicationDate;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private String coverImagePath;
    private String thumbnailPath;
    private String coverImageUrl;

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

    public Builder createdAt(LocalDateTime createdAt) {
      Objects.requireNonNull(createdAt);
      this.createdAt = createdAt;
      return this;
    }

    public Builder updatedAt(LocalDateTime updatedAt) {
      Objects.requireNonNull(updatedAt);
      this.updatedAt = updatedAt;
      return this;
    }
    public Builder coverImagePath(String coverImagePath) {
      Objects.requireNonNull(coverImagePath);
      this.coverImagePath = coverImagePath;
      return this;
    }

    public Builder thumbnailPath(String thumbnailPath) {
      Objects.requireNonNull(thumbnailPath);
      this.thumbnailPath = thumbnailPath;
      return this;
    }

    public Builder coverImageUrl(String coverImageUrl) {
      Objects.requireNonNull(coverImageUrl);
      this.coverImageUrl = coverImageUrl;
      return this;
    }

    public Book build() {
      return new Book(this);
    }
  }
}