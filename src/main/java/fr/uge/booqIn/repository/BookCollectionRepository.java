package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookCollectionRepository extends JpaRepository<BookCollection, Long> {
  Optional<BookCollection> findById(Long id);
  @Query("SELECT bc FROM BookCollection bc LEFT JOIN FETCH bc.books WHERE bc.id = :id")
  Optional<BookCollection> findByIdWithBooks(@Param("id") Long id);
  @Query("SELECT bc FROM BookCollection bc LEFT JOIN FETCH bc.books WHERE bc.owner = :owner")
  List<BookCollection> findByOwnerWithBooks(@Param("owner") User owner);
  Optional<BookCollection> findByTitle(String title);
  List<BookCollection> findByOwner(User owner);
  List<BookCollection> findByIsPublicTrue();
  Optional<BookCollection> findByIdAndOwner(Long id, User owner);
  //List<BookCollection> findByType(User owner);
  List<BookCollection> findByIsSmart(boolean isSmart);
  @Query("SELECT c FROM BookCollection c WHERE c.isPublic = true AND c.isSmart = true")
  List<BookCollection> findSmartCollections();
}
