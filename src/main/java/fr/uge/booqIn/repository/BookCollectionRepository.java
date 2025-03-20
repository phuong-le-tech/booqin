package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.GlobalBookCollection;
import fr.uge.booqIn.entity.SmartBookCollection;
import fr.uge.booqIn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing book collections.
 * Supports operations on the BookCollection hierarchy.
 */
public interface BookCollectionRepository extends JpaRepository<AbstractBookCollection, Long>,
        JpaSpecificationExecutor<AbstractBookCollection> {

  @Override
  @Query("SELECT bc FROM AbstractBookCollection bc LEFT JOIN FETCH bc.books WHERE bc.id = :id")
  @NonNull
  Optional<AbstractBookCollection> findById(@Param("id") Long id);

  @Query("SELECT bc FROM AbstractBookCollection bc LEFT JOIN FETCH bc.books WHERE bc.id = :id")
  Optional<AbstractBookCollection> findByIdWithBooks(@Param("id") Long id);

  @Query("SELECT bc FROM AbstractBookCollection bc LEFT JOIN FETCH bc.owner WHERE bc.owner = :user")
  List<AbstractBookCollection> findByOwnerWithBooks(@Param("user") User user);

  Optional<AbstractBookCollection> findByTitle(String title);

  List<AbstractBookCollection> findByOwner(User owner);

  @Query("SELECT bc FROM AbstractBookCollection bc LEFT JOIN FETCH bc.owner WHERE bc.isPublic = true")
  List<AbstractBookCollection> findByIsPublicTrue();

  @Query("SELECT bc FROM SmartBookCollection bc")
  List<SmartBookCollection> findAllSmartCollections();

  @Query("SELECT bc FROM GlobalBookCollection bc")
  List<GlobalBookCollection> findAllGlobalCollections();

  @Query("SELECT bc FROM AbstractBookCollection bc WHERE TYPE(bc) = :type AND bc.title = :title")
  Optional<AbstractBookCollection> findByTitleAndType(@Param("title") String title,
                                                      @Param("type") Class<? extends AbstractBookCollection> type);

  @Query("SELECT bc FROM GlobalBookCollection bc WHERE bc.title LIKE :title")
  Optional<GlobalBookCollection> findGlobalCollectionByTitle(@Param("title") String title);
}