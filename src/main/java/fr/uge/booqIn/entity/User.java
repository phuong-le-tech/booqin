package fr.uge.booqIn.entity;

import fr.uge.booqIn.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name="users")
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false)
  private Long version;

  @NotBlank(message = "First name is required")
  @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
  @Column(nullable = false)
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
  @Column(nullable = false)
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Column(nullable = false, unique = true)
  private String email;

  @NotBlank(message = "Password is required")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\w\\W]{8,}$",
          message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number")
  @Column(nullable = false)
  private String password;

  @Valid
  @Embedded
  private Address address;

  @NotNull(message = "User role is required")
  @Enumerated(EnumType.STRING)
  private UserRole role;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AbstractBookCollection> collections = new ArrayList<>();

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id")
  private Cart cart;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Date createdAt = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_at")
  private Date updatedAt = new Date();

  public User() {
  }

  public User(String firstName, String lastName, String email, String password, Address address, UserRole role) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.address = address;
    this.role = role;

    // Initialize default collections
    initializeDefaultCollections();
  }

  public User(String firstName, String lastName, String email, String password, Address address, UserRole role, Cart cart) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.address = address;
    this.role = role;
    this.cart = cart;

    // Initialize default collections
    initializeDefaultCollections();
  }

  /**
   * Initializes default collections for new users:
   * 1. Liked Books - Smart collection of books the user has liked
   * 2. Followed Users' Books - Smart collection of books from followed users' public collections
   */
  private void initializeDefaultCollections() {
    // Create "Liked Books" smart collection
    SmartBookCollection likedBooksCollection = new SmartBookCollection();
    likedBooksCollection.setTitle("Livres que j'ai aimés");
    likedBooksCollection.setDescription("Collection automatique des livres que j'ai aimés");
    likedBooksCollection.setOwner(this);
    likedBooksCollection.setPublic(false);
    likedBooksCollection.setCriteriaJson("{\"type\":\"liked_by_user\",\"userId\":" + this.id + "}");

    // Create "Followed Users' Books" smart collection
    SmartBookCollection followedUsersCollection = new SmartBookCollection();
    followedUsersCollection.setTitle("Livres des utilisateurs que je suis");
    followedUsersCollection.setDescription("Collection automatique des livres présents dans les collections publiques des utilisateurs que je suis");
    followedUsersCollection.setOwner(this);
    followedUsersCollection.setPublic(false);
    followedUsersCollection.setCriteriaJson("{\"type\":\"from_followed_users\",\"userId\":" + this.id + "}");

    // Add collections to user
    this.collections.add(likedBooksCollection);
    this.collections.add(followedUsersCollection);
  }

  public Long getId() {
    return id;
  }

  public Long getVersion() {
    return version;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getFullName() {
    return firstName + " " + lastName;
  }
  public String getEmail() {
    return email;
  }

  public Address getAddress() {
    return address;
  }

  public Cart getCart() {
    return cart;
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

  public List<AbstractBookCollection> getCollections() {
    return collections;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
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

  public void setId(long id) {
    this.id = id;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public void setCollections(List<AbstractBookCollection> collections) {
    this.collections = collections;
  }

  public void setCart(Cart cart) {
    this.cart = cart;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void addCollection(AbstractBookCollection collection) {
    collections.add(collection);
    collection.setOwner(this);
  }

  public void removeCollection(AbstractBookCollection collection) {
    collections.remove(collection);
  }

  public void addBookToCart(Book book) {
    cart.addBook(book);
  }

  /**
   * Gets the user's "Liked Books" collection
   * @return The smart collection of liked books or null if not found
   */
  public SmartBookCollection getLikedBooksCollection() {
    return collections.stream()
            .filter(c -> c instanceof SmartBookCollection && "Livres que j'ai aimés".equals(c.getTitle()))
            .map(c -> (SmartBookCollection) c)
            .findFirst()
            .orElse(null);
  }

  /**
   * Gets the user's "Followed Users' Books" collection
   * @return The smart collection of books from followed users or null if not found
   */
  public SmartBookCollection getFollowedUsersCollection() {
    return collections.stream()
            .filter(c -> c instanceof SmartBookCollection && "Livres des utilisateurs que je suis".equals(c.getTitle()))
            .map(c -> (SmartBookCollection) c)
            .findFirst()
            .orElse(null);
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return email.equals(user.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email);
  }
}