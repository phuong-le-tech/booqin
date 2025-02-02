package fr.uge.booqIn.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Book {

  @Id
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String author;

  @Column(nullable = false)
  private String isbn;

  @Column(nullable = false)
  private String description;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "publication_date")
  private Date publicationDate;

  @ManyToMany
  @JoinTable(
          name = "collection_books",
          joinColumns = @JoinColumn(name = "book_id"),
          inverseJoinColumns = @JoinColumn(name = "collection_id")
  )
  private List<BookCollection> collections = new ArrayList<>();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Date createdAt = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_at")
  private Date updatedAt = new Date();

  public Book() {
  }

  public Book(String title, String isbn, String description, Date publicationDate) {
    this.title = title;
    this.isbn = isbn;
    this.description = description;
    this.publicationDate = publicationDate;
  }

  public Book(String title, String author, String isbn, String description, Date publicationDate) {
    this.title = title;
    this.author = author;
    this.isbn = isbn;
    this.description = description;
    this.publicationDate = publicationDate;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthor() {
    return author;
  }

  public String getIsbn() {
    return isbn;
  }

  public String getDescription() {
    return description;
  }

  public Date getPublicationDate() {
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

  public void setId(Long id) {
    this.id = id;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setPublicationDate(Date publicationDate) {
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
}
