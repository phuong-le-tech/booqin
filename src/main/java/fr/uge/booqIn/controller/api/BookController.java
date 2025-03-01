package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.book.BookUpdate;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.factory.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {
  private static final Logger logger = LoggerFactory.getLogger(BookController.class);
  private final BookServiceInterface bookService;

  public BookController(BookServiceInterface bookService) {
    this.bookService = bookService;
  }

  @GetMapping("")
  public ResponseEntity<ApiResponse<Page<BookIndex>>> getBooks(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "id") String sortField,
          @RequestParam(defaultValue = "asc") String sortDirection,
          @RequestParam(required = false) String title,
          @RequestParam(required = false) String author,
          @RequestParam(required = false) String isbn) {

    logger.info("🔍 Recherche paginée des livres (page={}, size={}, sort={}, direction={})",
            page, size, sortField, sortDirection);

    Sort sort = sortDirection.equalsIgnoreCase("desc") ?
            Sort.by(sortField).descending() :
            Sort.by(sortField).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<BookIndex> books = bookService.findBooks(title, author, isbn, pageable);

    return ResponseEntity.ok(ApiResponse.success("Books found", books));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<BookShow>> getBookById(@PathVariable Long id) {
    logger.info("🔍 Recherche livre avec id: {}", id);

    BookShow bookShow = bookService.findById(id);
    return ResponseEntity.ok(ApiResponse.success("Book found", bookShow));
  }

  @PostMapping("")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Book>> createBook(@RequestBody Book book) {
    logger.info("🆕 Création d'un nouveau livre");

    Book createdBook = bookService.createBook(book);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Book created successfully", createdBook));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Book>> updateBook(
          @PathVariable Long id,
          @RequestBody Book book) {
    logger.info("🔄 Mise à jour du livre avec id: {}", id);

    book.setId(id);
    Book updatedBook = bookService.updateBook(book);
    return ResponseEntity.ok(
            ApiResponse.success("Book updated successfully", updatedBook));
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<BookShow>> patchBook(@PathVariable Long id, @RequestBody BookUpdate bookUpdate) {
    logger.info("🔄 Mise à jour partielle du livre avec id: {}", id);

    BookShow updatedBook = bookService.patchBook(id, bookUpdate);

    return ResponseEntity.ok(
            ApiResponse.success("Book updated successfully", updatedBook));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
    logger.info("🗑️ Suppression du livre avec id: {}", id);

    bookService.deleteBook(id);
    return ResponseEntity.ok(
            ApiResponse.success("Book deleted successfully"));
  }
}