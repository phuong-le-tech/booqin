package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.book.BookDto;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookDto>>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Page<BookDto> booksPage = bookService.findAllDto(PageRequest.of(page, size));

        // For pagination
        Map<String, Object> meta = new HashMap<>();
        meta.put("totalPages", booksPage.getTotalPages());
        meta.put("totalItems", booksPage.getTotalElements());
        meta.put("currentPage", page);

        return ResponseEntity.ok(
                ApiResponse.success("Books retrieved successfully", booksPage.getContent(), meta));
    }

    @GetMapping("/searchByTitle")
    public ResponseEntity<ApiResponse<List<BookDto>>> searchByTitle(@RequestParam String title) {
        List<BookDto> books = bookService.findBooksByTitleDto(title);
        return ResponseEntity.ok(
                ApiResponse.success("Books found for title: " + title, books));
    }

    @GetMapping("/searchByAuthor")
    public ResponseEntity<ApiResponse<List<BookDto>>> searchByAuthor(@RequestParam String author) {
        List<BookDto> books = bookService.findBooksByAuthorDto(author);
        return ResponseEntity.ok(
                ApiResponse.success("Books found for author: " + author, books));
    }

    @GetMapping("/searchByIsbn")
    public ResponseEntity<ApiResponse<BookDto>> searchByIsbn(@RequestParam String isbn) {
        Optional<BookDto> bookOptional = bookService.findByIsbnDto(isbn);
        return bookOptional
                .map(book -> ResponseEntity.ok(ApiResponse.success("Book found for ISBN: " + isbn, book)))
                .orElse(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No book found for ISBN: " + isbn)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookDto>> createBook(@RequestBody Book book) {
        BookDto savedBook = bookService.saveDto(book);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book created successfully", savedBook));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDto>> getBookById(@PathVariable Long id) {
        Optional<BookDto> bookOptional = bookService.findByIdDto(id);
        return bookOptional
                .map(book -> ResponseEntity.ok(ApiResponse.success("Book retrieved successfully", book)))
                .orElse(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Book not found with id: " + id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDto>> updateBook(
            @PathVariable Long id,
            @RequestBody Book book) {

        Optional<Book> existingBook = bookService.findById(id);
        if (existingBook.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Book not found with id: " + id));
        }

        book.setId(id);
        BookDto updatedBook = bookService.saveDto(book);
        return ResponseEntity.ok(
                ApiResponse.success("Book updated successfully", updatedBook));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        Optional<Book> existingBook = bookService.findById(id);
        if (existingBook.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Book not found with id: " + id));
        }

        bookService.delete(existingBook.get());
        return ResponseEntity.ok(
                ApiResponse.success("Book deleted successfully"));
    }
}