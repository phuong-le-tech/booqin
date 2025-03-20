package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.bookCollection.*;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
   * @param userId      L'ID du propriétaire des collections (optionnel)
   * @param pageable    Les informations de pagination
   * @return Une page de collections correspondant aux critères
   */
  Page<BookCollectionIndex> findBookCollections(String title, String description, Long userId, Pageable pageable);

  /**
   * Trouve une collection par son ID.
   *
   * @param id L'ID de la collection à trouver
   * @return La collection trouvée
   * @throws ResourceNotFoundException Si la collection n'existe pas
   */
  BookCollectionShow findById(@NotNull Long id) throws ResourceNotFoundException;

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
   * Récupère toutes les collections d'un utilisateur.
   *
   * @param user L'utilisateur dont on veut les collections
   * @return La liste des collections de l'utilisateur
   */
  List<BookCollectionShow> getUserCollections(User user);

  /**
   * Récupère toutes les collections publiques.
   *
   * @return La liste des collections publiques
   */
  List<BookCollectionShow> getPublicCollections();

  /**
   * Met à jour la collection des livres récemment ajoutés.
   */
  void addBookToCollection(Long id, Long bookId);

  void removeBookFromCollection(Long id, Long bookId);

  List<BookCollectionShow> getUserCollectionsWithBooks(User user);
}