package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

  /**
   * Finds a book by its ID with eager loading of ISBNs.
   *
   * @param id The unique identifier of the book
   * @return An optional containing the book with its ISBNs loaded
   */
  @Override
  @Query("SELECT DISTINCT b FROM Book b WHERE b.id = :id")
  @NonNull
  Optional<Book> findById(@NonNull Long id);

  /**
   * Finds a book by its ID with eager loading of authors.
   *
   * @param id The unique identifier of the book
   * @return An optional containing the book with its authors loaded
   */
  @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.authors WHERE b.id = :id")
  Optional<Book> findByIdWithAuthors(@NonNull Long id);

  /**
   * Finds a book by its ID with eager loading of ISBNs.
   *
   * @param id The unique identifier of the book
   * @return An optional containing the book with its ISBNs loaded
   */
  @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.isbns WHERE b.id = :id")
  Optional<Book> findByIdWithIsbns(@NonNull Long id);

  /**
   * Finds a book by its ID with eager loading of categories.
   *
   * @param id The unique identifier of the book
   * @return An optional containing the book with its categories loaded
   */
  @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.categories WHERE b.id = :id")
  Optional<Book> findByIdWithCategories(@NonNull Long id);

  /**
   * Finds a book by a specific ISBN.
   *
   * @param isbn The ISBN to search for
   * @return An optional containing the book if found
   */
  @Query("SELECT b FROM Book b JOIN FETCH b.isbns i WHERE i = :isbn")
  Optional<Book> findBookByIsbnInSet(String isbn);

  /**
   * Finds all books with optional specification and pagination.
   *
   * @param spec Optional specification for filtering books
   * @param pageable Pagination information
   * @return A page of books matching the specification
   */
  @Override
  Page<Book> findAll(@Nullable Specification<Book> spec, @NonNull Pageable pageable);

  /**
   * Finds books created after a specific date.
   *
   * @param date The cutoff date for book creation
   * @return A list of books created after the given date
   */
  List<Book> findByCreatedAtAfter(Date date);

  /**
   * Finds books containing a specific author.
   *
   * @param author The author to search for
   * @return A list of books by the given author
   */
  List<Book> findByAuthorsContaining(@NonNull Author author);

  /**
   * Finds books created after a specific date and time.
   *
   * @param cutoffDate The cutoff date and time for book creation
   * @return A list of books created after the given date and time
   */
  @Query("SELECT b FROM Book b WHERE b.createdAt >= :cutoffDate")
  List<Book> findByCreatedAtAfter(@NonNull LocalDateTime cutoffDate);

  /**
   * Finds books by a specific author's first and last name.
   *
   * @param firstName The first name of the author
   * @param lastName The last name of the author
   * @return A list of books by the author
   */
  @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.firstName = :firstName AND a.lastName = :lastName")
  List<Book> findByAuthor(
          @NonNull String firstName,
          @NonNull String lastName
  );

  @Modifying
  @Query("DELETE FROM AbstractBookCollection c WHERE :book MEMBER OF c.books")
  void deleteBookFromCollections(@Param("book") @NonNull Book book);

  @Modifying
  @Query("DELETE FROM Cart c WHERE :book MEMBER OF c.books")
  void deleteBookFromCarts(@Param("book") @NonNull Book book);

  @Modifying
  @Query("DELETE FROM BookLending l WHERE l.book = :book")
  void deleteBookFromLendings(@Param("book") @NonNull Book book);

  @Modifying
  @Query("DELETE FROM BookWaitingQueue q WHERE q.book = :book")
  void deleteBookFromQueues(@Param("book") @NonNull Book book);
}