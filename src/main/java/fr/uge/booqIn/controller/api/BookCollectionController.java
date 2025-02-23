package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.BookCollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/collections")
public class BookCollectionController {

  private static final Logger logger = Logger.getLogger(BookCollectionController.class.getName());
  private final BookCollectionService bookCollectionService;

  public BookCollectionController(BookCollectionService bookCollectionService) {
    this.bookCollectionService = bookCollectionService;
    logger.info("✅ CollectionService loaded !");
  }

  /**
   * Crée une collection (pas de vérif d’owner ici,
   * vous pouvez l’ajouter si vous voulez associer un user).
   */
  @PostMapping("/create")
  public ResponseEntity<ApiResponse<BookCollection>> createCollection(@RequestBody BookCollection bookCollection) {
    logger.info("✅ Creating a new collection");
    BookCollection created = bookCollectionService.createCollection(bookCollection);
    return ResponseEntity.ok(
            ApiResponse.success("Collection created successfully", created)
    );
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<BookCollection>> getCollection(@PathVariable Long id) {
    logger.info("✅ Fetch collection: " + id);

    Optional<BookCollection> collectionOpt = bookCollectionService.findCollectionById(id);
    return collectionOpt.map(bookCollection -> ResponseEntity.ok(
            ApiResponse.success("Collection found", bookCollection)
    )).orElseGet(() -> ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Collection not found")));

  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteCollection(
          @PathVariable Long id,
          @AuthenticationPrincipal User currentUser
  ) {
    logger.info("🗑️ Suppression de la collection avec id: " + id);

    // Vérifie l'existence
    Optional<BookCollection> collectionOpt = bookCollectionService.findCollectionById(id);
    if (collectionOpt.isEmpty()) {
      return ResponseEntity
              .status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("Collection not found"));
    }
    bookCollectionService.deleteCollection(id, currentUser);
    return ResponseEntity.ok(
            ApiResponse.success("Collection deleted successfully", null)
    );

  }

  @PostMapping("/{collectionId}/books/{bookId}")
  public ResponseEntity<ApiResponse<BookCollection>> addBookToCollection(
          @PathVariable Long collectionId,
          @PathVariable Long bookId,
          @AuthenticationPrincipal User currentUser
  ) {
    logger.info("📩 addBookToCollection (collection=" + collectionId + ", book=" + bookId + ")");

    // Vérifie la collection
    Optional<BookCollection> collectionOpt = bookCollectionService.findCollectionById(collectionId);
    if (collectionOpt.isEmpty()) {
      return ResponseEntity
              .status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("Collection not found"));
    }

    Optional<BookCollection> updated = bookCollectionService.addBookToCollection(collectionId, bookId, currentUser);
    return updated.map(bookCollection -> ResponseEntity.ok(
            ApiResponse.success("Book added to collection successfully", bookCollection)
    )).orElseGet(() -> ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Book not added to collection"))
    );

  }

  @DeleteMapping("/{collectionId}/books/{bookId}")
  public ResponseEntity<ApiResponse<BookCollection>> removeBookFromCollection(
          @PathVariable Long collectionId,
          @PathVariable Long bookId,
          @AuthenticationPrincipal User currentUser
  ) {
    logger.info("🗑️ removeBookFromCollection (collection=" + collectionId + ", book=" + bookId + ")");

    Optional<BookCollection> collectionOpt = bookCollectionService.findCollectionById(collectionId);
    if (collectionOpt.isEmpty()) {
      return ResponseEntity
              .status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("Collection not found"));
    }
    Optional<BookCollection> updated = bookCollectionService.removeBookFromCollection(collectionId, bookId, currentUser);
    return updated.map(bookCollection -> ResponseEntity.ok(
            ApiResponse.success("Book removed from collection successfully", bookCollection)
    )).orElseGet(() -> ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Book not removed from collection"))
    );

  }

  @GetMapping("/public")
  public ResponseEntity<ApiResponse<List<BookCollection>>> getPublicCollections() {
    logger.info("✅ Fetching public collections...");
    List<BookCollection> collections = bookCollectionService.getPublicCollections();
    return ResponseEntity.ok(
            ApiResponse.success("Public collections fetched", collections)
    );
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<BookCollection>> updateCollection(
          @PathVariable Long id,
          @AuthenticationPrincipal User currentUser,
          @RequestBody BookCollection updatedCollection
  ) {
    logger.info("User " + (currentUser == null ? "?" : currentUser.getId()) +
            " requested to update collection " + id);

    Optional<BookCollection> collectionOpt = bookCollectionService.findCollectionById(id);
    if (collectionOpt.isEmpty()) {
      return ResponseEntity
              .status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("Collection not found"));
    }

    Optional<BookCollection> updated = bookCollectionService.updateCollection(id, currentUser, updatedCollection);
    return updated.map(bookCollection -> ResponseEntity.ok(
            ApiResponse.success("Collection updated successfully", bookCollection)
    )).orElseGet(() -> ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Collection not updated"))
    );

  }
}