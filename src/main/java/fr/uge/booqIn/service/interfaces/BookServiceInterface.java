package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.book.BookUpdate;
import fr.uge.booqIn.entity.Book;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface BookServiceInterface {

  /**
   * Find books based on search criteria
   *
   * @param title Optional title filter
   * @param author Optional author filter
   * @param isbn Optional ISBN filter
   * @param pageable Pagination information
   * @return Page of BookIndex DTOs
   */
  Page<BookIndex> findBooks(String title, String author, String isbn, Pageable pageable);

  List<Book> findAll();

  /**
   * Find book by ID
   *
   * @param id Book ID
   * @return BookShow DTO
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if book not found
   */
  BookShow findById(@NotNull Long id);

  // Les méthodes de recherche spécifiques sont supprimées
  // et remplacées par la méthode findBooks avec pagination et filtres

  /**
   * Create new book
   *
   * @param book Book entity to create
   * @return Created book as Book
   * @throws jakarta.validation.ValidationException if validation fails
   */
  Book createBook(@NotNull @Valid Book book);

  /**
   * Save book from API
   *
   * @param book Book entity to create
   */
  void saveFromApi(@NotNull @Valid Book book);
  /**
   * Update book (all fields)
   *
   * @param book Book entity with updated values
   * @return Updated book as BookDto
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if book not found
   * @throws jakarta.validation.ValidationException if validation fails
   */
  Book updateBook(@NotNull @Valid Book book);

  /**
   * Partially update book with provided non-null fields
   *
   * @param id Book ID to update
   * @param bookUpdate DTO containing fields to update
   * @return Updated book as BookDto
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if book not found
   * @throws jakarta.validation.ValidationException if validation fails
   */
  BookShow patchBook(@NotNull Long id, @NotNull @Valid BookUpdate bookUpdate);

  /**
   * Delete book by ID
   *
   * @param id Book ID to delete
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if book not found
   */
  void deleteBook(@NotNull Long id);

  Book findBookById(@NotNull Long id);
}