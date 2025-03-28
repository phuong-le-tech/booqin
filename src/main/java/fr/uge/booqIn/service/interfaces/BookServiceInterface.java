package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.book.BookCreate;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.book.BookUpdate;
import fr.uge.booqIn.entity.Book;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Service interface for book operations in the BooqIn application.
 * Provides methods for searching, creating, updating, and deleting books.
 */
@Validated
public interface BookServiceInterface {

  /**
   * Find books based on search criteria with pagination.
   *
   * @param title Optional title filter (can be null)
   * @param author Optional author filter (can be null)
   * @param isbn Optional ISBN filter (can be null)
   * @param pageable Pagination and sorting information
   * @return Page of BookIndex DTOs matching the criteria
   */
  Page<BookIndex> findBooks(String title, String author, String isbn, @NonNull Pageable pageable);

  /**
   * Retrieves all books in the system.
   *
   * @return List of all Book entities
   */
  List<Book> findAll();

  /**
   * Find a specific book by its ID.
   *
   * @param id Book ID to find
   * @return BookShow DTO containing the book details
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if book not found
   */
  BookShow findById(@NonNull Long id);

  /**
   * Create a new book in the system.
   *
   * @param book Book entity to create
   * @return Created book entity with generated ID
   * @throws jakarta.validation.ValidationException if validation fails
   */
  Book createBook(@NonNull @Valid Book book);

  Book createFromDto(@NonNull @Valid BookCreate dto);


  /**
   * Saves a book retrieved from external API into the system.
   * This method is specifically designed for importing books from external sources.
   *
   * @param book Book entity to save from external API
   * @throws jakarta.validation.ValidationException if validation fails
   */
  void saveFromApi(@NonNull Book book);

  /**
   * Update all fields of an existing book.
   *
   * @param book Book entity with updated values (must include ID)
   * @return Updated book entity
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if book not found
   * @throws jakarta.validation.ValidationException if validation fails
   */
  Book updateBook(@NonNull @Valid Book book);

  /**
   * Partially update a book with only the provided non-null fields.
   *
   * @param id Book ID to update
   * @param bookUpdate DTO containing only the fields to update
   * @return Updated book as BookShow DTO
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if book not found
   * @throws jakarta.validation.ValidationException if validation fails
   */
  BookShow patchBook(@NonNull Long id, @NonNull @Valid BookUpdate bookUpdate);

  /**
   * Delete a book by its ID.
   *
   * @param id Book ID to delete
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if book not found
   */
  void deleteBook(@NonNull Long id);

  /**
   * Find the raw Book entity by its ID.
   * This method returns the entity itself rather than a DTO.
   *
   * @param id Book ID to find
   * @return Book entity
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if book not found
   */
  Book findBookById(@NonNull Long id);
}