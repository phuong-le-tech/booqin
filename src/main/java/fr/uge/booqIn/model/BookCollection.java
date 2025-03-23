package fr.uge.booqIn.model;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface BookCollection extends PolymorphicEntity, Commentable, Votable, Followable {

  /**
   * Obtient l'identifiant unique de la collection.
   *
   * @return L'identifiant de la collection
   */
  Long getId();

  /**
   * Obtient la version de la collection (pour le verrouillage optimiste).
   *
   * @return La version de la collection
   */
  Long getVersion();

  /**
   * Obtient le titre de la collection.
   *
   * @return Le titre de la collection
   */
  String getTitle();

  /**
   * Obtient la description de la collection.
   *
   * @return La description de la collection
   */
  String getDescription();

  /**
   * Obtient le propriétaire de la collection.
   *
   * @return Le propriétaire de la collection
   */
  User getOwner();

  /**
   * Obtient la liste des livres dans la collection.
   *
   * @return La liste des livres
   */
  List<Book> getBooks();

  /**
   * Vérifie si la collection est publique.
   *
   * @return true si la collection est publique, false sinon
   */
  boolean isPublic();

  /**
   * Obtient la date de création de la collection.
   *
   * @return La date de création
   */
  LocalDateTime getCreatedAt();

  /**
   * Obtient la date de dernière mise à jour de la collection.
   *
   * @return La date de dernière mise à jour
   */
  LocalDateTime getUpdatedAt();

  /**
   * Obtient le type de la collection.
   *
   * @return Le type de la collection
   */
  CollectionType getCollectionType();
  

  /**
   * Définit l'identifiant de la collection.
   *
   * @param id L'identifiant à définir
   */
  void setId(Long id);

  /**
   * Définit la version de la collection.
   *
   * @param version La version à définir
   */
  void setVersion(Long version);

  /**
   * Définit le titre de la collection.
   *
   * @param title Le titre à définir
   */
  void setTitle(String title);

  /**
   * Définit la description de la collection.
   *
   * @param description La description à définir
   */
  void setDescription(String description);

  /**
   * Définit le propriétaire de la collection.
   *
   * @param owner Le propriétaire à définir
   */
  void setOwner(User owner);

  /**
   * Définit la liste des livres de la collection.
   *
   * @param books La liste des livres à définir
   */
  void setBooks(List<Book> books);

  /**
   * Définit si la collection est publique.
   *
   * @param isPublic true si la collection doit être publique, false sinon
   */
  void setPublic(boolean isPublic);

  /**
   * Définit la date de création de la collection.
   *
   * @param createdAt La date de création à définir
   */
  void setCreatedAt(LocalDateTime createdAt);

  /**
   * Définit la date de dernière mise à jour de la collection.
   *
   * @param updatedAt La date de dernière mise à jour à définir
   */
  void setUpdatedAt(LocalDateTime updatedAt);

  /**
   * Ajoute un livre à la collection.
   *
   * @param book Le livre à ajouter
   */
  void addBook(Book book);

  /**
   * Retire un livre de la collection.
   *
   * @param book Le livre à retirer
   * @throws IllegalArgumentException Si le livre n'est pas dans la collection
   */
  void removeBook(Book book);

  /**
   * Énumération des types de collections.
   */
  enum CollectionType {
    NORMAL, // collection basique
    SMART,  // définie par des critères, par l'utilisateur
    GLOBAL  // générée automatiquement
  }
}