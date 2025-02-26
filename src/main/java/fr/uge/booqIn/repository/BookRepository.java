package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
  @Query("select b from Book b where b.title = :title")
  List<Book> findBooksByTitle(String title);

  @Query("SELECT b FROM Book b JOIN b.authors a "
          + "WHERE a.firstName = :author OR a.lastName = :author")
  List<Book> findBooksByAuthor(String author);

  @Query("SELECT b FROM Book b JOIN b.isbns i WHERE i = :isbn")
  Optional<Book> findBookByIsbnInSet(String isbn);

  @Query("select b from Book b where b.publicationDate = :publishedDate")
  List<Book> findByPublishedYear(String publishedDate);
  
  


}
