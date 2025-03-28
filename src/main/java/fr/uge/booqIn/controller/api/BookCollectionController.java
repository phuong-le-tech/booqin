package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.bookCollection.BookCollectionCreate;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/collections")
public class BookCollectionController {

  private final BookCollectionServiceInterface bookCollectionService;

  public BookCollectionController(BookCollectionServiceInterface bookCollectionService) {
    this.bookCollectionService = bookCollectionService;
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
          @RequestParam(required = false) boolean isPublic) {

    Sort sort = sortDirection.equalsIgnoreCase("desc") ?
            Sort.by(sortField).descending() :
            Sort.by(sortField).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<BookCollectionIndex> collections = bookCollectionService.findBookCollections(title,
            description,
            isPublic,
            pageable);
    return ResponseEntity.ok(ApiResponse.success("Collections found", collections));
  }

  @PostMapping("")
  public ResponseEntity<ApiResponse<BookCollectionShow>> createCollection(
          @Valid @RequestBody BookCollectionCreate bookCollectionCreateDto,
          @AuthenticationPrincipal User currentUser) {
    BookCollectionShow collectionShow = bookCollectionService.createBookCollection(bookCollectionCreateDto, currentUser);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Collection created successfully", collectionShow));
  }

  @GetMapping("/{id}")
  @PreAuthorize("@collectionSecurity.canAccessCollection(#id) or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<BookCollectionShow>> getCollection(@PathVariable Long id) {
    BookCollectionShow collection = bookCollectionService.findBookCollectionById(id);
    return ResponseEntity.ok(ApiResponse.success("Collection found", collection));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@collectionSecurity.canAccessCollection(#id) or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> deleteCollection(
          @PathVariable Long id) {

    bookCollectionService.deleteBookCollection(id);
    return ResponseEntity.ok(ApiResponse.success("Collection deleted successfully"));
  }

  @PatchMapping("/{id}")
  @PreAuthorize("@collectionSecurity.isCollectionOwner(#id) or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<BookCollectionShow>> updateBookCollection(
          @PathVariable Long id,
          @Valid @RequestBody BookCollectionUpdate bookCollectionUpdate) {

    BookCollectionShow updated = bookCollectionService.updateBookCollection(id, bookCollectionUpdate);
    return ResponseEntity.ok(ApiResponse.success("Collection updated successfully", updated));
  }

  @PostMapping("/{collectionId}/books/{bookId}")
  public ResponseEntity<ApiResponse<BookCollectionShow>> addBookToCollection(
          @PathVariable Long collectionId,
          @PathVariable Long bookId) {

    bookCollectionService.addBookToCollection(collectionId, bookId);
    return ResponseEntity.ok(ApiResponse.success("Book added to collection successfully"));
  }

  @DeleteMapping("/{collectionId}/books/{bookId}")
  @PreAuthorize("@collectionSecurity.isCollectionOwner(#collectionId) or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<BookCollectionShow>> removeBookFromCollection(
          @PathVariable Long collectionId,
          @PathVariable Long bookId) {

    bookCollectionService.removeBookFromCollection(collectionId, bookId);
    return ResponseEntity.ok(ApiResponse.success("Book removed from collection successfully"));
  }

  @GetMapping("/public")
  public ResponseEntity<ApiResponse<Page<BookCollectionIndex>>> getPublicCollections(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "id") String sortField,
          @RequestParam(defaultValue = "asc") String sortDirection) {

    Sort sort = sortDirection.equalsIgnoreCase("desc") ?
            Sort.by(sortField).descending() :
            Sort.by(sortField).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<BookCollectionIndex> collections = bookCollectionService.getPublicCollections(pageable);
    return ResponseEntity.ok(ApiResponse.success("Public collections fetched", collections));
  }

  @GetMapping("/user/public")
  public ResponseEntity<ApiResponse<Page<BookCollectionIndex>>> getPublicUserCollection(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "id") String sortField,
          @RequestParam(defaultValue = "asc") String sortDirection,
          @AuthenticationPrincipal User currentUser) {

    Sort sort = sortDirection.equalsIgnoreCase("desc") ?
            Sort.by(sortField).descending() :
            Sort.by(sortField).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<BookCollectionIndex> collections = bookCollectionService.getPublicUserCollection(currentUser, pageable);
    return ResponseEntity.ok(ApiResponse.success("Public user collection", collections));
  }

  @GetMapping("/user/private")
  public ResponseEntity<ApiResponse<Page<BookCollectionIndex>>> getPrivateUserCollection(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "id") String sortField,
          @RequestParam(defaultValue = "asc") String sortDirection,
          @AuthenticationPrincipal User currentUser) {

    Sort sort = sortDirection.equalsIgnoreCase("desc") ?
            Sort.by(sortField).descending() :
            Sort.by(sortField).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<BookCollectionIndex> collections = bookCollectionService.getPrivateUserCollection(currentUser, pageable);
    return ResponseEntity.ok(ApiResponse.success("Private user collection", collections));
  }

  @GetMapping("/user/books")
  public ResponseEntity<ApiResponse<Page<BookCollectionShow>>> getUserCollectionsWithBooks(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "id") String sortField,
          @RequestParam(defaultValue = "asc") String sortDirection,
          @AuthenticationPrincipal User currentUser) {

    Sort sort = sortDirection.equalsIgnoreCase("desc") ?
            Sort.by(sortField).descending() :
            Sort.by(sortField).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<BookCollectionShow> collections = bookCollectionService.getUserCollectionsWithBooks(currentUser, pageable);
    return ResponseEntity.ok(ApiResponse.success("User collections with books fetched", collections));
  }
}