package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

  @Query("select b from Book b where b.title = :title")
  List<Book> findBooksByTitle(String title);

  @Query("SELECT DISTINCT b FROM Book b JOIN FETCH b.authors a WHERE a.firstName = :author OR a.lastName = :author")
  List<Book> findBooksByAuthor(String author);

  @Query("SELECT b FROM Book b JOIN FETCH b.isbns i WHERE i = :isbn")
  Optional<Book> findBookByIsbnInSet(String isbn);

  @Query("select b from Book b where b.publicationDate = :publishedDate")
  List<Book> findByPublishedYear(String publishedDate);

  @Query("SELECT b FROM Book b JOIN FETCH b.authors ")
  List<Book> findAllWithAuthors();

  @Query("SELECT b FROM Book b JOIN FETCH b.authors WHERE b.title LIKE %:title%")
  List<Book> findBooksByTitleWithAuthors(String title);

  @Override
  @Query("select b from Book b join fetch b.authors join fetch b.isbns" )
  @NonNull
  List<Book> findAll();

  @Query(value = "SELECT DISTINCT b.id FROM Book b",
      countQuery = "SELECT COUNT(DISTINCT b.id) FROM Book b")
  Page<Long> findAllBookIds(Pageable pageable);

  @Query("SELECT b FROM Book b LEFT JOIN FETCH b.authors LEFT JOIN FETCH b.isbns WHERE b.id IN :ids")
  List<Book> findAllByIdIn(@Param("ids") List<Long> ids);
  
  
  @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.authors LEFT JOIN FETCH b.isbns WHERE b.id = :id")
  Optional<Book> findByIdWithIsbns(@Param("id") Long id);
  
  @Query("SELECT b.isbns FROM Book b WHERE b.id = :id")
  Set<String> findIsbnsByBookId(@Param("id") Long id);
  
  
}
