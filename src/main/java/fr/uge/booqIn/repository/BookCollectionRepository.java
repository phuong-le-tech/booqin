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
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCollectionRepository extends JpaRepository<AbstractBookCollection, Long>, JpaSpecificationExecutor<AbstractBookCollection>, CrudRepository<AbstractBookCollection, Long> {

  /**
   * Find all collections by owner with pagination
   */
  Page<AbstractBookCollection> findAllByOwner(User owner, Pageable pageable);

  /**
   * Find all public collections with pagination
   */
  Page<AbstractBookCollection> findByIsPublicTrue(Pageable pageable);

  /**
   * Find all collections by owner with eager loading of books
   */
  @Query("SELECT c FROM AbstractBookCollection c JOIN FETCH c.books WHERE c.owner = ?1")
  List<AbstractBookCollection> findByOwnerWithBooks(User owner);

  @Query("SELECT c FROM AbstractBookCollection c JOIN FETCH c.books WHERE c.id = ?1")
  Optional<AbstractBookCollection> findByIdWithBooks(Long id);

  /**
   * Find public collections by owner with pagination
   */
  @Query("SELECT c FROM AbstractBookCollection c WHERE c.owner = :owner AND c.isPublic = true")
  Page<AbstractBookCollection> findPublicCollectionsByOwner(User owner, Pageable pageable);

  @Query("SELECT c FROM AbstractBookCollection c WHERE c.owner = :owner AND c.isPublic = false")
  Page<AbstractBookCollection> findPrivateCollectionsByOwner(User owner, Pageable pageable);


  Optional<AbstractBookCollection> findByTitle(String title);

  @Query("SELECT c FROM SmartBookCollection c")
  List<SmartBookCollection> findAllSmartCollections();

  /**
   * Find all public collections by owner without pagination
   * Used for selecting a random collection when a user follows another user
   */
  @Query("SELECT c FROM AbstractBookCollection c WHERE c.owner.id = :ownerId AND c.isPublic = true")
  List<AbstractBookCollection> findPublicCollectionsByOwnerId(Long ownerId);

  @Query("SELECT c FROM LikedBookCollection c WHERE c.owner.id = :ownerId")
  Optional<AbstractBookCollection> findLikedCollectionByOwnerId(Long ownerId);

  @Query("SELECT c FROM FollowedBookCollection c WHERE c.owner.id = :ownerId")
  Optional<AbstractBookCollection> findFollowedCollectionByOwnerId(Long ownerId);

  @Query("SELECT c FROM RecentBookCollection c")
  Optional<AbstractBookCollection> findFirstRecentBookCollection();
}