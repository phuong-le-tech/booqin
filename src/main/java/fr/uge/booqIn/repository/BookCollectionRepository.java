package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface BookCollectionRepository extends JpaRepository<BookCollection, Long>, JpaSpecificationExecutor<BookCollection> {
  @Override
  @Query("SELECT bc FROM BookCollection bc LEFT JOIN FETCH bc.books WHERE bc.id = :id")
  @NonNull
  Optional<BookCollection> findById(@Param("id") Long id);

  @Query("SELECT bc FROM BookCollection bc LEFT JOIN FETCH bc.books WHERE bc.id = :id")
  Optional<BookCollection> findByIdWithBooks(@Param("id") Long id);

  @Query("SELECT bc FROM BookCollection bc LEFT JOIN FETCH bc.owner WHERE bc.owner = :user")
  List<BookCollection> findByOwnerWithBooks(@Param("user") User user);

  Optional<BookCollection> findByTitle(String title);

  List<BookCollection> findByOwner(User owner);

  @Query("SELECT bc FROM BookCollection bc LEFT JOIN FETCH bc.owner WHERE bc.isPublic = true")
  List<BookCollection> findByIsPublicTrue();

  @Query("SELECT bc FROM BookCollection bc LEFT JOIN FETCH bc.owner WHERE bc.isSmart = true")
  List<BookCollection> findByIsSmart(boolean isSmart);

  @Query("SELECT c FROM BookCollection c WHERE c.isPublic = true AND c.isSmart = true")
  List<BookCollection> findSmartCollections();

  Optional<BookCollection> findByTitleAndCollectionType(String title, BookCollection.CollectionType collectionType);

  List<BookCollection> findByCollectionType(BookCollection.CollectionType collectionType);

}
