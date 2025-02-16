package fr.uge.booqIn.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.uge.booqIn.enums.UserRole;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="users")
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Embedded
  private Address address;

  @Enumerated(EnumType.STRING)
  private UserRole role;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
  private List<ToLendBook> booksToLend;


  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
  private List<BookCollection> collections;


  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Date createdAt = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_at")
  private Date updatedAt = new Date();

  public User() {
  }

  public User(String fistName, String lastName, String email, String password, Address address, UserRole role) {
    this.firstName = fistName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.address = address;
    this.role = role;
  }

  public Long getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public Address getAddress() {
    return address;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (this.role == UserRole.ADMIN) {
      return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
    }
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  public UserRole getRole() {
    return role;
  }

  public List<ToLendBook> getBooksToLend() {
    return booksToLend;
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

  public void setFirstName(String fistName) {
    this.firstName = fistName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public void setBooksToLend(List<ToLendBook> toBorrowBooks) {
    this.booksToLend = toBorrowBooks;
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
    collection.setOwner(this);
  }

  public void removeCollection(BookCollection collection) {
    collections.remove(collection);
  }

}
