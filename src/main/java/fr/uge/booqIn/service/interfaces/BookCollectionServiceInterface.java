package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.bookCollection.*;
import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.interfaces.BookCollection;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;

public interface BookCollectionServiceInterface {

  /**
   * Search for collections based on specific criteria.
   *
   * @param title       The title to search for (optional)
   * @param description The description to search for (optional)
   * @param pageable    The pagination information
   * @return A page of collections matching the criteria
   */
  Page<BookCollectionIndex> findBookCollections(String title,
                                                String description,
                                                boolean isPublic,
                                                @NonNull Pageable pageable);

  /**
   * Find a collection by its ID.
   *
   * @param id The ID of the collection to find
   * @return The found collection
   * @throws ResourceNotFoundException If the collection doesn't exist
   */
  BookCollectionShow findBookCollectionById(@NonNull Long id) throws ResourceNotFoundException;

  /**
   * Create a new collection.
   *
   * @param collection The collection to create
   * @return The created collection
   */
  BookCollectionShow createBookCollection(@NonNull @Valid BookCollectionCreate collection, User currentUser);

  /**
   * Update an existing collection.
   *
   * @param id         The ID of the collection to update
   * @param collection The update data
   * @return The updated collection
   */
  BookCollectionShow updateBookCollection(@NonNull Long id, @NonNull @Valid BookCollectionUpdate collection);

  /**
   * Delete a collection.
   *
   * @param id The ID of the collection to delete
   */
  void deleteBookCollection(@NonNull Long id);

  /**
   * Retrieve all public collections with pagination.
   *
   * @param pageable The pagination information
   * @return A page of public collections
   */
  Page<BookCollectionIndex> getPublicCollections(@NonNull Pageable pageable);

  /**
   * Add a book to a collection.
   */
  void addBookToCollection(@NonNull Long id, @NonNull Long bookId);

  /**
   * Remove a book from a collection.
   */
  void removeBookFromCollection(@NonNull Long id, @NonNull Long bookId);

  /**
   * Retrieve a user's collections with their books (with pagination).
   *
   * @param user     The user whose collections we want
   * @param pageable The pagination information
   * @return A page of the user's collections with their books
   */
  Page<BookCollectionShow> getUserCollectionsWithBooks(@NonNull User user, @NonNull Pageable pageable);

  /**
   * Find a collection by its ID.
   */
  BookCollection findCollectionById(@NonNull Long id);

  /**
   * Retrieve the public collections of a specific user with pagination.
   *
   * @param owner    The owner of the collections
   * @param pageable The pagination information
   * @return A page of the owner's public collections
   */
  Page<BookCollectionIndex> getPublicUserCollection(@NonNull User owner, @NonNull Pageable pageable);

  /**
   * Retrieve the private collections of a specific user with pagination.
   *
   * @param owner    The owner of the collections
   * @param pageable The pagination information
   * @return A page of the owner's private collections
   */
  Page<BookCollectionIndex> getPrivateUserCollection(@NonNull User owner, @NonNull Pageable pageable);


  List<AbstractBookCollection> findPublicCollectionsByOwnerId(@NonNull Long ownerId);

  AbstractBookCollection findLikedCollectionByOwnerId(@NonNull Long ownerId);
}