package fr.uge.booqIn.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name="users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded
  private Person person;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String role;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
  private List<ToLendBook> booksToLend;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
  private List<Collection> collections;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Date createdAt = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_at")
  private Date updatedAt = new Date();

  public User() {
  }

  public User(Person person, String password, String role) {
    this.person = person;
    this.password = password;
    this.role = role;
  }

  public Long getId() {
    return id;
  }

  public Person getPerson() {
    return person;
  }

  public String getPassword() {
    return password;
  }

  public String getRole() {
    return role;
  }

  public List<ToLendBook> getBooksToLend() {
    return booksToLend;
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

  public void setPerson(Person person) {
    this.person = person;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public void setBooksToLend(List<ToLendBook> toBorrowBooks) {
    this.booksToLend = toBorrowBooks;
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
    collection.setOwner(this);
  }

  public void removeCollection(Collection collection) {
    collections.remove(collection);
  }

}
