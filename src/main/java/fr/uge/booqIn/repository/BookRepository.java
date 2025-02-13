package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Book save(Book book);

    Optional<Book> findById(Long id);

    List<Book> findAll();

    void delete(Book book);

    List<Book> findByTitle(String title);

    List<Book> findByAuthors(String author);

    Optional<Book> findByIdIsbn(String isbn);

    List<Book> findByPublishedYear(String publishedDate);

}
