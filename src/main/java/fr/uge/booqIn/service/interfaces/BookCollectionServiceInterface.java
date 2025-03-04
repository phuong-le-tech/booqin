package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface BookCollectionServiceInterface {

  /**
   * Find book collections based on search criteria
   *
   * @param name Optional name filter
   * @param description Optional description filter
   * @param userId Optional user ID filter
   * @param pageable Pagination information
   * @return Page of BookCollectionIndex DTOs
   */
  Page<BookCollectionIndex> findBookCollections(String name, String description, Long userId, Pageable pageable);

  /**
   * Find book collection by ID
   *
   * @param id BookCollection ID
   * @return BookCollectionShow DTO
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if collection not found
   */
  BookCollectionShow findById(@NotNull Long id);

  /**
   * Create new book collection
   *
   * @param bookCollection BookCollection entity to create
   * @return Created book collection as BookCollection
   * @throws jakarta.validation.ValidationException if validation fails
   */
  BookCollection createBookCollection(@NotNull @Valid BookCollection bookCollection);

  /**
   * Update book collection (all fields)
   *
   * @param bookCollection BookCollection entity with updated values
   * @return Updated book collection as BookCollection
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if collection not found
   * @throws jakarta.validation.ValidationException if validation fails
   */
  BookCollection updateBookCollection(@NotNull @Valid BookCollection bookCollection);

  /**
   * Partially update book collection with provided non-null fields
   *
   * @param id BookCollection ID to update
   * @param bookCollectionUpdate DTO containing fields to update
   * @return Updated book collection as BookCollectionShow
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if collection not found
   * @throws jakarta.validation.ValidationException if validation fails
   */
  BookCollectionShow patchBookCollection(@NotNull Long id, @NotNull @Valid BookCollectionUpdate bookCollectionUpdate);

  /**
   * Delete book collection by ID
   *
   * @param id BookCollection ID to delete
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if collection not found
   * @throws org.springframework.security.access.AccessDeniedException if user is not authorized
   */
  void deleteBookCollection(@NotNull Long id);

  /**
   * Add a book to a collection
   *
   * @param collectionId ID of the collection to add the book to
   * @param bookId ID of the book to add
   * @return Updated BookCollection entity
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if collection or book not found
   * @throws org.springframework.security.access.AccessDeniedException if current user is not the owner
   */
  BookCollection addBookToCollection(@NotNull Long collectionId, @NotNull Long bookId);

  /**
   * Remove a book from a collection
   *
   * @param collectionId ID of the collection to remove the book from
   * @param bookId ID of the book to remove
   * @param currentUser Current authenticated user
   * @return Updated BookCollection entity
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if collection or book not found
   * @throws org.springframework.security.access.AccessDeniedException if current user is not the owner
   */
  BookCollection removeBookFromCollection(@NotNull Long collectionId, @NotNull Long bookId, User currentUser);

  /**
   * Get all collections belonging to a user
   *
   * @param user User entity
   * @return List of BookCollection entities
   */
  List<BookCollection> getUserCollections(User user);

  /**
   * Get all collections belonging to a user with their books eagerly loaded
   *
   * @param user User entity
   * @return List of BookCollection entities with books
   */
  List<BookCollection> getUserCollectionsWithBooks(User user);

  /**
   * Get all public collections
   *
   * @return List of public BookCollection entities
   */
  List<BookCollection> getPublicCollections();

  /**
   * Get a specific book from a collection
   *
   * @param collectionId ID of the collection
   * @param bookId ID of the book
   * @return Book entity if found, null otherwise
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if collection not found
   */
  Book getBookFromCollection(Long collectionId, Long bookId);
}