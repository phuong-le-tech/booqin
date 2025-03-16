package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.bookCollection.BookCollectionCreate;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.mapper.UserMapper;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/collections")
public class BookCollectionController {

  private static final Logger logger = Logger.getLogger(BookCollectionController.class.getName());
  private final BookCollectionServiceInterface bookCollectionService;
  private final BookCollectionMapper bookCollectionMapper = new BookCollectionMapper();

  public BookCollectionController(BookCollectionServiceInterface bookCollectionService) {
    this.bookCollectionService = bookCollectionService;
    logger.info("✅ CollectionService loaded !");
  }

  @GetMapping("")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Page<BookCollectionIndex>>> getAllBookCollections(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "id") String sortField,
          @RequestParam(defaultValue = "asc") String sortDirection,
          @RequestParam(required = false) String title,
          @RequestParam(required = false) String description,
          @RequestParam(required = false) Long userId) {

    Sort sort = sortDirection.equalsIgnoreCase("desc") ?
            Sort.by(sortField).descending() :
            Sort.by(sortField).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<BookCollectionIndex> collections = bookCollectionService.findBookCollections(title, description, userId, pageable);

    return ResponseEntity.ok(ApiResponse.success("Collections found", collections));
  }

  /**
   * Create a new collection
   */
  @PostMapping("")
  public ResponseEntity<ApiResponse<BookCollectionShow>> createCollection(
          @Valid @RequestBody BookCollectionCreate bookCollectionCreateDto,
          @AuthenticationPrincipal User currentUser) {

    logger.info("✅ Creating a new collection for user: " + currentUser.getUsername());
    BookCollection bookCollection = new BookCollection();
    bookCollection.setTitle(bookCollectionCreateDto.title());
    bookCollection.setDescription(bookCollectionCreateDto.description());
    bookCollection.setPublic(bookCollectionCreateDto.isPublic());
    bookCollection.setOwner(currentUser);

    BookCollection createdCollection = bookCollectionService.createBookCollection(bookCollection);
    BookCollectionShow collectionShow = bookCollectionMapper.toBookCollectionShow(createdCollection);

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Collection created successfully", collectionShow));
  }

  /**
   * Get a collection by ID
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<BookCollectionShow>> getCollection(@PathVariable Long id) {
    logger.info("✅ Fetch collection: " + id);


    BookCollectionShow collection = bookCollectionService.findById(id);
    return ResponseEntity.ok(ApiResponse.success("Collection found", collection));
  }

  /**
   * Delete a collection
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteCollection(
          @PathVariable Long id,
          @AuthenticationPrincipal User currentUser) {

    logger.info("🗑️ Deleting collection with id: " + id);

    bookCollectionService.deleteBookCollection(id);
    return ResponseEntity.ok(ApiResponse.success("Collection deleted successfully"));
  }

  @PutMapping("")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<BookCollection>> updateCollection(@RequestBody BookCollection bookCollection) {

    BookCollection updatedBookCollection = bookCollectionService.updateBookCollection(bookCollection);

    return ResponseEntity.ok(ApiResponse.success("BookCollection updated successfully", updatedBookCollection));
  }

  /**
   * Update a collection (partial update)
   */
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<BookCollectionShow>> patchCollection(
          @PathVariable Long id,
          @AuthenticationPrincipal User currentUser,
          @Valid @RequestBody BookCollectionUpdate bookCollectionUpdate) {

    logger.info("📝 Updating collection " + id + " by user " + currentUser.getUsername());

    BookCollectionShow updated = bookCollectionService.patchBookCollection(id, bookCollectionUpdate);
    return ResponseEntity.ok(ApiResponse.success("Collection updated successfully", updated));
  }

  /**
   * Add a book to a collection
   */
  @PostMapping("/{collectionId}/books/{bookId}")
  public ResponseEntity<ApiResponse<BookCollectionShow>> addBookToCollection(
          @PathVariable Long collectionId,
          @PathVariable Long bookId) {

    logger.info("📚 Adding book " + bookId + " to collection " + collectionId);

    BookCollection updated = bookCollectionService.addBookToCollection(collectionId, bookId);
    BookCollectionShow updatedShow = bookCollectionMapper.toBookCollectionShow(updated);
    return ResponseEntity.ok(ApiResponse.success("Book added to collection successfully", updatedShow));
  }

  /**
   * Remove a book from a collection
   */
  @DeleteMapping("/{collectionId}/books/{bookId}")
  public ResponseEntity<ApiResponse<BookCollectionShow>> removeBookFromCollection(
          @PathVariable Long collectionId,
          @PathVariable Long bookId,
          @AuthenticationPrincipal User currentUser) {

    logger.info("🗑️ Removing book " + bookId + " from collection " + collectionId);

    BookCollection updated = bookCollectionService.removeBookFromCollection(collectionId, bookId, currentUser);
    BookCollectionShow updatedShow = bookCollectionMapper.toBookCollectionShow(updated);
    return ResponseEntity.ok(ApiResponse.success("Book removed from collection successfully", updatedShow));
  }

  /**
   * Get all public collections
   */
  @GetMapping("/public")
  public ResponseEntity<ApiResponse<List<BookCollectionShow>>> getPublicCollections() {
    logger.info("✅ Fetching public collections");
    List<BookCollectionShow> collections = bookCollectionService.getPublicCollections();
    return ResponseEntity.ok(ApiResponse.success("Public collections fetched", collections));
  }

  /**
   * Get all collections for the current user
   */
  @GetMapping("/user")
  public ResponseEntity<ApiResponse<List<BookCollectionShow>>> getUserCollections(
          @AuthenticationPrincipal User currentUser) {

    logger.info("✅ Fetching collections for user: " + currentUser.getUsername());

    List<BookCollectionShow> collections = bookCollectionService.getUserCollectionsWithBooks(currentUser);
    System.out.println("FETCHED USER COLLECTIONS: " + collections);
    return ResponseEntity.ok(ApiResponse.success("User collections fetched", collections));
  }

  /**
   * Get all collections with books for the current user
   */
  /*@GetMapping("/user/with-books")
  public ResponseEntity<ApiResponse<List<BookCollectionShow>>> getUserCollectionsWithBooks(
          @AuthenticationPrincipal User currentUser) {
    logger.info("✅ Fetching collections with books for user: " + currentUser.getUsername());

    List<BookCollectionShow> collections = bookCollectionService.getUserCollectionsWithBooks(currentUser);
    return ResponseEntity.ok(ApiResponse.success("User collections with books fetched", collections));
  }
*/
  /**
   * Get a specific book from a collection
   */
  @GetMapping("/{collectionId}/books/{bookId}")
  public ResponseEntity<ApiResponse<Book>> getBookFromCollection(
          @PathVariable Long collectionId,
          @PathVariable Long bookId) {

    logger.info("📖 Fetching book " + bookId + " from collection " + collectionId);
    Book book = bookCollectionService.getBookFromCollection(collectionId, bookId);
    return ResponseEntity.ok(ApiResponse.success("Book fetched", book));
  }

}