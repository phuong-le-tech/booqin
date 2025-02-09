package fr.uge.booqIn.entity;

import fr.uge.booqIn.Adapter.BookAPIs.GoogleAPI.IndustryIdentifier;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class Book {

  @Id
  @Column()
  private String ibsn10;

  @Id
  @Column
  private String ibsn13;


  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String author;


  @Column(nullable = false)
  private String description;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "publication_date")
  private String publicationDate;

  @ManyToMany
  private List<BookCollection> collections;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Date createdAt = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_at")
  private Date updatedAt = new Date();

  public Book() {
  }

  private String extractIdentifier(List<IndustryIdentifier> identifiers, String type) {
    return identifiers.stream()
            .filter(id -> type.equalsIgnoreCase(id.type()))
            .map(IndustryIdentifier::identifier)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Identifier de type " + type + " non trouvé"));
  }

  public Book(String title, String author, List<IndustryIdentifier> industryIdentifiers, String description, String publicationDate) {
    this.title = title;
    this.author = author;
    this.ibsn10 = extractIdentifier(industryIdentifiers, "ISBN_10");
    this.ibsn13 = extractIdentifier(industryIdentifiers, "ISBN_13");
    this.description = description;
    this.publicationDate = publicationDate;
  }

  public String getIbsn10() {
    return ibsn10;
  }

  public String getIbsn13() {
    return ibsn13;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthor() {
    return author;
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

  public void setAuthor(String author) {
    this.author = author;
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
    collections.add(collection);
    collection.getBooks().add(this);
  }

  public void removeCollection(BookCollection collection) {
    collections.remove(collection);
    collection.getBooks().remove(this);
  }

  @Override
  public String toString() {
    return "Book{" +
            "ibsn10='" + ibsn10 + '\'' +
            ", ibsn13='" + ibsn13 + '\'' +
            ", title='" + title + '\'' +
            ", author='" + author + '\'' +
            ", description='" + description + '\'' +
            ", publicationDate='" + publicationDate + '\'' +
            '}';
  }
}