package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.bookCollection.*;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.model.BookCollection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface commune pour tous les services de gestion de collections de livres.
 * Définit les opérations CRUD et les fonctionnalités de base communes à tous les types de collections.
 */
public interface BookCollectionServiceInterface {

  /**
   * Recherche des collections selon des critères spécifiques.
   *
   * @param title       Le titre à rechercher (optionnel)
   * @param description La description à rechercher (optionnel)
   * @param pageable    Les informations de pagination
   * @return Une page de collections correspondant aux critères
   */
  Page<BookCollectionIndex> findBookCollections(String title, String description, boolean isPublic, Pageable pageable);

  /**
   * Trouve une collection par son ID.
   *
   * @param id L'ID de la collection à trouver
   * @return La collection trouvée
   * @throws ResourceNotFoundException Si la collection n'existe pas
   */
  BookCollectionShow findBookCollectionById(@NotNull Long id) throws ResourceNotFoundException;

  /**
   * Crée une nouvelle collection.
   *
   * @param collection  La collection à créer
   * @return La collection créée
   */
  BookCollectionShow createBookCollection(@NotNull @Valid BookCollectionCreate collection, User currentUser);

  /**
   * Met à jour une collection existante.
   *
   * @param id          L'ID de la collection à mettre à jour
   * @param collection  Les données de mise à jour
   * @return La collection mise à jour
   */
  BookCollectionShow updateBookCollection(@NotNull Long id, @NotNull @Valid BookCollectionUpdate collection);

  /**
   * Supprime une collection.
   *
   * @param id          L'ID de la collection à supprimer
   */
  void deleteBookCollection(@NotNull Long id);

  /**
   * Récupère toutes les collections publiques avec pagination.
   *
   * @param pageable Les informations de pagination
   * @return Une page des collections publiques
   */
  Page<BookCollectionIndex> getPublicCollections(Pageable pageable);

  /**
   * Ajoute un livre à une collection.
   */
  void addBookToCollection(Long id, Long bookId);

  /**
   * Supprime un livre d'une collection.
   */
  void removeBookFromCollection(Long id, Long bookId);

  /**
   * Récupère les collections d'un utilisateur avec leurs livres (avec pagination).
   *
   * @param user L'utilisateur dont on veut les collections
   * @param pageable Les informations de pagination
   * @return Une page des collections de l'utilisateur avec leurs livres
   */
  Page<BookCollectionShow> getUserCollectionsWithBooks(User user, Pageable pageable);

  /**
   * Trouve une collection par son ID.
   */
  BookCollection findCollectionById(Long id);

  /**
   * Récupère les collections publiques d'un utilisateur spécifique avec pagination.
   *
   * @param owner Le propriétaire des collections
   * @param pageable Les informations de pagination
   * @return Une page des collections publiques du propriétaire
   */
  Page<BookCollectionIndex> getPublicUserCollection(User owner, Pageable pageable);
  Page<BookCollectionIndex> getPrivateUserCollection(User owner, Pageable pageable);




}