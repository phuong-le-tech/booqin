package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.SmartBookCollection;
import fr.uge.booqIn.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCollectionRepository extends
        JpaRepository<AbstractBookCollection, Long>,
        JpaSpecificationExecutor<AbstractBookCollection>,
        CrudRepository<AbstractBookCollection, Long> {

  /**
   * Retrieves all book collections owned by a specific user with pagination.
   *
   * @param owner The user whose collections are to be retrieved
   * @param pageable Pagination information
   * @return A page of book collections owned by the user
   */
  Page<AbstractBookCollection> findAllByOwner(@NonNull User owner, @NonNull Pageable pageable);

  /**
   * Retrieves all public book collections with pagination.
   *
   * @param pageable Pagination information
   * @return A page of public book collections
   */
  Page<AbstractBookCollection> findByIsPublicTrue(@NonNull Pageable pageable);

  /**
   * Finds all book collections for a specific owner with their books eagerly loaded.
   *
   * @param owner The user whose collections are to be retrieved
   * @return A list of book collections with their books loaded
   */
  @Query("SELECT c FROM AbstractBookCollection c LEFT JOIN FETCH c.books WHERE c.owner = ?1")
  List<AbstractBookCollection> findByOwnerWithBooks(@NonNull User owner);

  /**
   * Finds a specific book collection by ID with its books eagerly loaded.
   *
   * @param id The ID of the book collection
   * @return An optional containing the book collection with its books loaded
   */
  @Query("SELECT c FROM AbstractBookCollection c LEFT JOIN FETCH c.books WHERE c.id = ?1")
  Optional<AbstractBookCollection> findByIdWithBooks(@NonNull Long id);

  /**
   * Retrieves public book collections for a specific owner with pagination.
   *
   * @param owner The user whose public collections are to be retrieved
   * @param pageable Pagination information
   * @return A page of public book collections owned by the user
   */
  @Query("SELECT c FROM AbstractBookCollection c WHERE c.owner = :owner AND c.isPublic = true")
  Page<AbstractBookCollection> findPublicCollectionsByOwner(@NonNull User owner, @NonNull Pageable pageable);

  /**
   * Retrieves private book collections for a specific owner with pagination.
   *
   * @param owner The user whose private collections are to be retrieved
   * @param pageable Pagination information
   * @return A page of private book collections owned by the user
   */
  @Query("SELECT c FROM AbstractBookCollection c WHERE c.owner = :owner AND c.isPublic = false")
  Page<AbstractBookCollection> findPrivateCollectionsByOwner(@NonNull User owner, @NonNull Pageable pageable);

  /**
   * Finds a book collection by its exact title.
   *
   * @param title The exact title of the book collection
   * @return An optional containing the book collection if found
   */
  Optional<AbstractBookCollection> findByTitle(@NonNull String title);

  /**
   * Retrieves all smart book collections.
   *
   * @return A list of all smart book collections
   */
  @Query("SELECT c FROM SmartBookCollection c")
  List<SmartBookCollection> findAllSmartCollections();

  /**
   * Finds all public book collections for a specific user by their ID.
   *
   * @param ownerId The ID of the user
   * @return A list of public book collections for the user
   */
  @Query("SELECT c FROM AbstractBookCollection c WHERE c.owner.id = :ownerId AND c.isPublic = true")
  List<AbstractBookCollection> findPublicCollectionsByOwnerId(@NonNull Long ownerId);

  /**
   * Finds the liked book collection for a specific user by their ID.
   *
   * @param ownerId The ID of the user
   * @return An optional containing the liked book collection
   */
  @Query("SELECT c FROM LikedBookCollection c WHERE c.owner.id = :ownerId")
  Optional<AbstractBookCollection> findLikedCollectionByOwnerId(@NonNull Long ownerId);

  /**
   * Finds the followed book collection for a specific user by their ID.
   *
   * @param ownerId The ID of the user
   * @return An optional containing the followed book collection
   */
  @Query("SELECT c FROM FollowedBookCollection c WHERE c.owner.id = :ownerId")
  Optional<AbstractBookCollection> findFollowedCollectionByOwnerId(@NonNull Long ownerId);

  /**
   * Finds the first recent book collection.
   *
   * @return An optional containing the first recent book collection
   */
  @Query("SELECT c FROM RecentBookCollection c")
  Optional<AbstractBookCollection> findFirstRecentBookCollection();
}