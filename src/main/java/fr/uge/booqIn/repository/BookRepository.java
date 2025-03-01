package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

  @Override
  @Query("SELECT DISTINCT b FROM Book b " +
          "LEFT JOIN FETCH b.authors " +
          "LEFT JOIN FETCH b.isbns " +
          "LEFT JOIN FETCH b.collections " +
          "WHERE b.id = :id")
  @NonNull
  Optional<Book> findById(@NonNull Long id);

  @Query("SELECT b FROM Book b JOIN FETCH b.isbns i WHERE i = :isbn")
  Optional<Book> findBookByIsbnInSet(String isbn);

  @Override
  @Query("select b from Book b join fetch b.authors join fetch b.isbns" )
  @NonNull
  List<Book> findAll();
}
