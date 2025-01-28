package fr.uge.booqIn.Entity;

import jakarta.persistence.*;

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
  private List<Collection> collections;

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

  public List<Collection> getCollections() {
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

  public void setCollections(List<Collection> collections) {
    this.collections = collections;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void addCollection(Collection collection) {
    collections.add(collection);
    collection.getBooks().add(this);
  }

  public void removeCollection(Collection collection) {
    collections.remove(collection);
    collection.getBooks().remove(this);
  }


}
