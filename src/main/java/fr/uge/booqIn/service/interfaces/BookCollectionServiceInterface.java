package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.entity.BookCollection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

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
   */
  void deleteBookCollection(@NotNull Long id);
}