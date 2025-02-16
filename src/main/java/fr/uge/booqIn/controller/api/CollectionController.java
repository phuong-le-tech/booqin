package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.CollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/collections")
public class CollectionController {
  private static final Logger logger = Logger.getLogger(CollectionController.class.getName());
  private final CollectionService collectionService;

  public CollectionController(CollectionService collectionService) {
    this.collectionService = collectionService;
    logger.info("✅ CollectionService loaded !");
  }

  @PostMapping("/create")
  public ResponseEntity<BookCollection> createCollection(@RequestBody BookCollection bookCollection) {
    logger.info("✅ Collection created !");
    return ResponseEntity.ok(collectionService.createCollection(bookCollection));
  }


  @GetMapping("/{id}")
  public ResponseEntity<BookCollection> getCollection(@PathVariable Long id) {
    return ResponseEntity.ok(collectionService.getCollectionById(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCollection(
          @PathVariable Long id,
          @AuthenticationPrincipal User currentUser
  ) {
    collectionService.deleteCollection(id, currentUser);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{collectionId}/books/{bookId}")
  public ResponseEntity<BookCollection> addBookToCollection(
          @PathVariable Long collectionId,
          @PathVariable Long bookId,
          @AuthenticationPrincipal User currentUser
  ) {
    try {
      var updated = collectionService.addBookToCollection(collectionId, bookId, currentUser);
      logger.info("✅ Book added to collection: " + collectionId);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      logger.warning("❌ Error adding book to collection: " + e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (AccessDeniedException e) {
      logger.warning("❌ Access denied: " + e.getMessage());
      return ResponseEntity.status(403).build();
    }
  }

  @DeleteMapping("/{collectionId}/books/{bookId}")
  public ResponseEntity<BookCollection> removeBookFromCollection(
          @PathVariable Long collectionId,
          @PathVariable Long bookId,
          @AuthenticationPrincipal User currentUser
  ) {
    try {
      var updated = collectionService.removeBookFromCollection(collectionId, bookId, currentUser);
      logger.info("✅ Book removed from collection: " + collectionId);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      logger.warning("❌ Error removing book from collection: " + e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (AccessDeniedException e) {
      logger.warning("❌ Access denied: " + e.getMessage());
      return ResponseEntity.status(403).build();
    }
  }

  @GetMapping("/public")
  public ResponseEntity<List<BookCollection>> getPublicCollections() {
    try {
      var collections = collectionService.getPublicCollections();
      logger.info("✅ Public collections fetched ! (" + collections.size() + " collections");
      return ResponseEntity.ok(collections);
    } catch (IllegalArgumentException e) {
      logger.warning("❌ Error fetching public collections: " + e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @PatchMapping("/{id}")
  public ResponseEntity<BookCollection> updateCollection(
          @PathVariable Long id,
          @AuthenticationPrincipal User currentUser,
          @RequestBody BookCollection updatedCollection
  ) {
    try {
      logger.info("User " + currentUser.getId() + " requested to update collection " + id);
      BookCollection collection = collectionService.updateCollection(id, currentUser, updatedCollection);
      logger.info("✅ Collection \" + id + \" successfully updated");
      return ResponseEntity.ok(collection);
    } catch (AccessDeniedException e) {
      logger.warning("❌ Access denied for updating collection " + id);
      return ResponseEntity.status(403).build();
    } catch (IllegalArgumentException e) {
      logger.warning("❌ Error updating collection " + id + ": " + e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }
}
