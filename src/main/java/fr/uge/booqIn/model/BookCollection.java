package fr.uge.booqIn.model;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface BookCollection extends PolymorphicEntity, Commentable, Votable, Followable {

  Long getId();

  Long getVersion();

  String getTitle();

  String getDescription();

  User getOwner();

  List<Book> getBooks();

  boolean isPublic();

  LocalDateTime getCreatedAt();

  LocalDateTime getUpdatedAt();

  CollectionType getCollectionType();

  void setId(Long id);

  void setVersion(Long version);

  void setTitle(String title);

  void setDescription(String description);

  void setOwner(User owner);

  void setBooks(List<Book> books);

  void setPublic(boolean isPublic);

  void setCreatedAt(LocalDateTime createdAt);

  void setUpdatedAt(LocalDateTime updatedAt);

  void addBook(Book book);

  void removeBook(Book book);

  enum CollectionType {
    NORMAL,
    SMART,
    GLOBAL,
    LIKED,
    FOLLOWED,
    RECENT,
    AUTHOR,
  }
}