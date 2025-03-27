package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.book.BookUpdate;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    private final BookServiceInterface bookService;
    private final ImageService imageService;
    private final BookMapper bookMapper = new BookMapper();
    @Value("${file.upload-dir:./data/images/}")
    private String uploadDir;
    
    public BookController(BookServiceInterface bookService, ImageService imageService) {
        this.bookService = bookService;
        this.imageService = imageService;
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
    @PreAuthorize("hasRole('USER')")
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

    @PostMapping("/{id}/cover")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookShow>> uploadCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        logger.info("📸 Upload d'une couverture pour le livre avec id: {}", id);

        try {
            var book = bookService.findBookById(id);
            var imagePath = imageService.saveBookCover(file, id);
            book.setCoverImagePath(imagePath);

            var updatedBook = bookService.updateBook(book);
            var bookShow = bookMapper.toBookShow(updatedBook);

            return ResponseEntity.ok(
                    ApiResponse.success("Book cover uploaded successfully", bookShow));
        } catch (IOException e) {
            logger.error("Failed to upload image", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload image: " + e.getMessage()));
        }
    }

    @GetMapping("/images/{*path}")
    public ResponseEntity<Resource> serveImage(@PathVariable String path) {
        logger.info("Serving image: {}", path);

        try {
            var imagePath = Paths.get(uploadDir, path).normalize();
            var uploadDirPath = Paths.get(uploadDir).normalize();

            if (!imagePath.startsWith(uploadDirPath)) {
                logger.warn("Tentative de directory traversal détectée: {}", path);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            var resource = new UrlResource(imagePath.toUri());

            if (resource.exists()) {
                var headers = new HttpHeaders();
                headers.setCacheControl("max-age=86400"); // 24 hours cache

                var contentType = Files.probeContentType(imagePath);
                if (contentType == null) {
                    var fileName = imagePath.getFileName().toString().toLowerCase();
                    if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                        contentType = "image/jpeg";
                    } else if (fileName.endsWith(".png")) {
                        contentType = "image/png";
                    } else if (fileName.endsWith(".gif")) {
                        contentType = "image/gif";
                    } else {
                        contentType = "application/octet-stream";
                    }
                }

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .headers(headers)
                        .body(resource);
            } else {
                logger.warn("Image non trouvée: {}", imagePath);
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            logger.error("Erreur lors de l'accès à l'image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
